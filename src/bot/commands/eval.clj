(defn evaluate [body message]
  (when (:debug env/env)
    (eval (read-string body))))
