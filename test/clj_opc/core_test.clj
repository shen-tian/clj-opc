(ns clj-opc.core-test
  (:require [clojure.test :refer :all]
            [clj-opc.core :refer :all]
            [gloss.core :as gloss]
            [gloss.io :as io]))

(deftest protocol-test
  (testing "Testing basics of the protocol"
    (let [x [0 0 [0 0 0]]]
      (is (= x 
             (io/decode opc-protocol
                        (io/encode opc-protocol x)))))
    (let [y [0 0 [0 255 0 255 0 255]]]
      (is (= y
             (io/decode opc-protocol
                        (io/encode opc-protocol y)))))))
