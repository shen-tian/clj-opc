(ns clj-opc.core
  (:require
   [manifold.deferred :as d]
   [manifold.stream :as s]
   [clojure.edn :as edn]
   [aleph.tcp :as tcp]
   [gloss.core :as gloss]
   [gloss.io :as io]))

(def opc-protocol
  (gloss/compile-frame
   [:ubyte 
    :ubyte
    :ubyte
    :ubyte
    :ubyte
    :ubyte
    :ubyte
    :ubyte]))

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
