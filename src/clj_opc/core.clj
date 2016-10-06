(ns clj-opc.core
  (:require
   [manifold.deferred :as d]
   [manifold.stream :as s]
   [aleph.tcp :as tcp]
   [gloss.core :as gloss]
   [gloss.io :as io]))

(def opc-protocol
  (gloss/compile-frame
   [:ubyte 
    :ubyte
    (gloss/repeated :ubyte :prefix :uint16)]))

;; Definitely don't need the duplex stream stuff here...
(defn wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
      (s/map #(io/encode protocol %) out)
      s)
    (s/splice
      out
      (io/decode-stream s protocol))))

(defn client
  [host port]
  (d/chain (tcp/client {:host host, :port port})
           #(wrap-duplex-stream opc-protocol %)))

(defn make-opc-packed
  [color n]
  (merge [0 0] 
         (reduce into (repeat n [(:red color)
                                 (:green color)
                                 (:blue color)]))))

(defn show 
  [client color n]
  (s/put! client (make-opc-packed color n)))
