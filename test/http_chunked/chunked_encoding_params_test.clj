(ns http-chunked.chunked-encoding-params-test
  (:import [java.io.ByteArrayOutputStream]
           [java.io.ByteArrayInputStream])
  (:require [clojure.test :refer :all]
            [http-chunked.request :refer :all]))

;; from stackoverflow..
(defn make-byte-array [inp]
  "Convert an input stream is to byte array"
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (let [byte-arr (byte-array 1000)]
      (loop [n (.read inp byte-arr 0 1000)]
        (when (> n 0)
          (.write out byte-arr 0 n)
          (recur (.read inp byte-arr 0 1000))))
      (.toByteArray out))))

(defn str->bais
  [string]
  (java.io.ByteArrayInputStream. (.getBytes string)))

(defn make-request
  [body-str use-chunked-encoding?] 
  (let [request {:headers []
                 :body (str->bais body-str)}]
       (if use-chunked-encoding? 
         (merge {:headers ["transfer-encoding" "chunked"]} request))
          request))

(deftest test-extract-on-well-formed
  (testing "Extraction of properties on well formed input."
    (let [input-str "4\r\n1234\r\n0\r\n"
          request (make-request input-str true)
          chunk-map (extract-chunked-map request)]
      (is (= 4 (:size chunk-map)))
      (is (= [49 50 51 52] 
             (into [] (make-byte-array (:data chunk-map)))))
      (is (= "0" (:end chunk-map))))))

(deftest test-wrapper-on-non-chunked-req
  (testing "Request should not contain key :chunked-encoding-params when the transfer-encoding header is not set."
    (let [wrapped-req (wrap-chunked-encoding-params (make-request "4\r\n1234\r\n\0\r\n" false))]
      (is (nil? (:chunked-encoding-params wrapped-req))))))
