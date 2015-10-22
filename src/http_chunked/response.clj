(ns http-chunked.response)

(def clrf-bytes (byte-array [(byte \return) (byte \newline)]))

(def tail-bytes (.getBytes (Long/toHexString 0)))

(def tail-byte-buffer (java.nio.ByteBuffer/wrap (byte-array (into [] (concat tail-bytes clrf-bytes clrf-bytes)))))

(defn make-size-bytes [chunk]
  (.getBytes (Long/toHexString (alength chunk))))

(defn request-chunked? 
  [request]
  (= (get (:headers request) "transfer-encoding") "chunked"))

(defn encode-chunk 
  [chunk]
  (let [size-bytes (make-size-bytes chunk)]
    (byte-array (into [] (concat size-bytes clrf-bytes chunk clrf-bytes)))))

(defn encode-chunk-if
  [chunk expr & args]
  (if (expr args) (encode-chunk) chunk))

(defn send-trailer!
  "Sends the trailing 0/r/n/r/n bytes and closes the channel."
  [channel]
  (send! channel {:status 200 
                  :body tail-byte-buffer}
                  true)))