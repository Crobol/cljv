(defn evaluate [body message]
  (if (:debug env/env)
    (eval (read-string body))
    (str "Evaluate is only available in debug mode.")))
