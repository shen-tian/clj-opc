# clj-opc

[![Build Status](https://travis-ci.org/shen-tian/clj-opc.svg?branch=master)](https://travis-ci.org/shen-tian/clj-opc)
[![Clojars Project](https://img.shields.io/clojars/v/clj-opc.svg)](https://clojars.org/clj-opc)

Open Pixel Control client for Clojure

## Usages

Include, in your `project.clj`:

    [clj-opc "0.1.0"]

Example code:

    (use 'clj-opc.core)
    
    (def opc (client "127.0.0.1" 7890 1000))
    (show! opc [{:r 255 :g 0 :b 255}])
    (put! opc [0 0 [255 0 255]])
    (close! opc)

The last param in 'client' is delay before trying. The client will
accept data without making a live connection, and will always try to connect.
Once a connectin is established to an OPC server, it will start transmitting.
    


