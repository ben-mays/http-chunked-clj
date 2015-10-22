(ns http-chunked.request)

(defn extract-chunked-map
  [request]
  nil)

(defn wrap-chunked-encoding-params
  "Middleware that converts the a chunked-encoding request :body into a map with :size, :data and :end keys. Keys are placed in the request object under :chunked-encoding-params."
  [handler]
  (fn [request]
    (let [transfer-encoding-header (get (:headers request) "transfer-encoding" nil)]
      (if (= "chunked" transfer-encoding-header)
        (handler (assoc request :chunked-encoding-params (extract-chunked-map request)))
        (handler request)))))
