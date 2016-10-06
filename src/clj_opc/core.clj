(ns clj-opc.core
  (:require
   [manifold.deferred :as d]
   [manifold.stream :as s]
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
                 (map #(vector (:red %) (:green %) (:blue %)) 
                      colors))))

;; Definitely don't need the duplex stream stuff here...
(defn- wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
      (s/map #(io/encode protocol %) out)
      s)
    (s/splice out (io/decode-stream s protocol))
    ))

(defn client
  [host port]
  (d/chain (tcp/client {:host host, :port port})
           #(wrap-duplex-stream opc-protocol %)))

(defn show!
  [client colors]
  (s/put! client (make-color-struct colors)))

(defn kill! [client] (s/close! client))
