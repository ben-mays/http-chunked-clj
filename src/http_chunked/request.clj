(ns http-chunked.request)

(defn request-chunked? 
  [request]
  (= (get (:headers request) "transfer-encoding") "chunked"))

(defn extract-chunked-map
  [request]
  (let [inp (clojure.java.io/input-stream (:body request))
        rdr (clojure.java.io/reader inp)
        lines (reduce conj [] (line-seq rdr))
        chunked-params {:size (nth lines 0)
                        :body (nth lines 1)
                        :end (nth lines 2)}]
    (println chunked-params)
    chunked-params))

(defn wrap-chunked-encoding-params
  "Middleware that converts the a chunked-encoding request :body into a map with :size, :data and :end keys. Keys are placed in the request object under :chunked-encoding-params."
  [handler]
  (fn [request]
    (if (request-chunked? request)
      (handler (assoc request :chunked-encoding-params (extract-chunked-map request)))
      (handler request))))
