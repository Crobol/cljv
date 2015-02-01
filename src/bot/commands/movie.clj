(def ^:private omdbapi-url-template "http://www.omdbapi.com/?t=%s")

(def ^:private movie-options [["-p" "--plot"]])

(defn movie
  "Query for information about a specific movie by title"
  {:aliases ["imdb" "tv" "series"] :options movie-options}
  [query message]
  (when (not (clojure.string/blank? query))
    (let [opts (cli/parse-opts (clojure.string/split query #"\s") movie-options)
          url (format omdbapi-url-template (url-encode (clojure.string/join " " (:arguments opts))))
          raw-json (:body (http/get url))
          json (json/read-str raw-json)
          genres (clojure.string/join " | "(clojure.string/split (get json "Genre") #", "))
          movie-url (str "http://imdb.com/title/" (get json "imdbID"))]
      (cond
       (flag? opts :plot) (get json "Plot")
       :else (str (get json "Title") " (" (get json "Year") ") " (get json "imdbRating") " ★, " genres " -- " movie-url)))
    )
  )
