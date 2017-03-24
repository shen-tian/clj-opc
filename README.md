# clj-opc

[![Build Status](https://travis-ci.org/shen-tian/clj-opc.svg?branch=master)](https://travis-ci.org/shen-tian/clj-opc)
[![Clojars Project](https://img.shields.io/clojars/v/clj-opc.svg)](https://clojars.org/clj-opc)

A [Open Pixel Control](http://openpixelcontrol.org/) client for Clojure.

## Usages

If using `lein`, include, in your `project.clj`:

    [clj-opc "0.1.1"]

Example code:

````clojure
(use 'clj-opc.core)
    
(def opc (client "127.0.0.1" 7890 1000))
(show! opc [{:r 255 :g 0 :b 255} {:r 0 :g 255 :b 0}]) ;; purple and green.
(put! opc [0 0 [255 0 255 0 255 0]]) ;; and again
(close! opc)
````

The last param in 'client' is delay before trying. The client will
accept data without making a live connection, and will always try to connect.
Once a connectin is established to an OPC server, it will start transmitting. Thus,
though it doesn't provide feedback on connects and disconnects except by writing to
console, it is pretty "fire and forget".
    
The `show!` function takes a vector of rgb maps.

The `put!` function takes something closer closer to the raw OPC protocol:

    [channel cmd [r0 g0 b0 r1 g1 b1 ... ]]

Note that it's not necessary to indicate the length of the LED data with bytes 2 and 3,
as this is handled by the client. 

