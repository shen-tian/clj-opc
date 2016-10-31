(ns clj-opc.core
  (:require
   [manifold.deferred :as d]
   [manifold.stream :as s]
   [manifold.time :as t]
   [aleph.tcp :as tcp]
   [gloss.core :as gloss]
   [gloss.io :as io]))

;; The OPC protocol. Thanks gloss!
(def opc-protocol
  (gloss/compile-frame
   [:ubyte 
    :ubyte
    (gloss/repeated :ubyte :prefix :uint16)]))

(defn- make-color-struct
  [colors]
  (merge [0 0] 
         (reduce into 
                 (map #(vector (:r %) (:g %) (:b %)) 
                      colors))))

(defn- wrap-duplex-stream
  [protocol s]
  "somwhat template code"
  (let [out (s/stream)]
    (s/connect
      (s/map #(io/encode protocol %) out)
      s)
    (s/splice out (io/decode-stream s protocol))
    ))

(defn- opc-client
  [host port]
  (d/chain (tcp/client {:host host, :port port})
              #(wrap-duplex-stream opc-protocol %)))

(defn- set-discard [state]
  (assoc state :in
         (let [s (if (contains? state :in) 
                   (:in state) 
                   (s/stream))]
           (s/consume (fn [x] nil) s)
           s)))

(defn- set-transmit [state out]
  (assoc state :in
         (let [s (if (contains? state :in)
                   (:in state)
                   (s/stream))]
           (s/connect s out)
           s)))

(defn- try-connect [host port delay state]
  (if (not (:stop @state))  
    (let [attempt (t/in delay #(opc-client host port))]
      (d/on-realized 
       attempt
       (fn [conn]
         (d/on-realized 
          conn 
          (fn [s] 
            (do (prn "Connected")
                (swap! state #(set-transmit % s))
                (s/on-drained 
                 s #(do (swap! state set-discard)
                        (prn "Disconnected")
                        (try-connect host port delay state)))))
          (fn [s]
            (do (try-connect host port delay state)))))
       (fn [conn]
         (prn "When should we get here?"))))))

(defn client  
  "creates a new opc client. Defaults to localhost:7890 and 1s retry" 
  ([]
   (client "localhost" 7890))
  ([host port]
   (client host port 1000))
  ([host port delay]   
   (let [state (atom {})]
     (do
       (prn (str "Connecting to " host ":" port))
       (swap! state set-discard)
       (try-connect host port delay state)
       state))))

(defn put! 
  "Use this with a raw struct to be encoded"
  [client color-struct]
  (s/put! (:in  @client) color-struct))

(defn close! 
  "Closes the client"
  [client]
  (do 
    (swap! client #(assoc % :stop true))
    (s/close! (:in @client))))

(defn show! 
  "Show colors. Encoded as a coll of maps, each with the :r, :g and :b keys"
  [client colors]
  (put! client (make-color-struct colors)))
