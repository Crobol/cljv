(def ^:private weather-api-key (:weather-api-key env/env))
(def ^:private weather-default-location (:weather-default-location env/env))
(def ^:private temperature-unit "°C")
(def ^:private wind-speed-unit "m/s")

(defn- degrees-to-direction [degrees]
  (cond
   (< degrees 22.5) "N"
   (< degrees 67.5) "NE"
   (< degrees 112.6) "E"
   (< degrees 157.5) "SE"
   (< degrees 202.5) "S"
   (< degrees 247.5) "SW"
   (< degrees 292.5) "W"
   (< degrees 337.5) "NW"
   (<= degrees 360) "N"
   ))

(defn- to-one-decimal [number]
  (float (/ (Math/round (* number 10)) 10)))

(defn- search-location [location]
  (let
      [geocode-url (str "https://maps.googleapis.com/maps/api/geocode/json?address=" location "&sensor=true")
       result (:body (http/get geocode-url))
       json (json/read-str result)]
      (if (not (nil? (get json "results")))
        (let
          [first-result (first (get json "results"))]
          { :longitude (get-in first-result ["geometry" "location" "lng"])
            :latitude (get-in first-result ["geometry" "location" "lat"])
            :formatted-location (get-in first-result ["formatted_address"]) }
          )))
  )

(defn weajew
  "Look up the weather at the specified location"
  ([] (weajew weather-default-location))
  ([location-name message]
    (let [location (search-location (if (clojure.string/blank? location-name) weather-default-location location-name))
          weather-api-url (str "https://api.forecast.io/forecast/" weather-api-key "/" (:latitude location) "," (:longitude location) "?units=si")
          weather-result (:body (http/get weather-api-url))
          weather-json (json/read-str weather-result)
          weather (get weather-json "currently")
          temperature (to-one-decimal (get weather "temperature"))
          apparent-temperature (to-one-decimal (get weather "apparentTemperature"))
          conditions (get weather "summary")
          wind-direction (degrees-to-direction (get weather "windBearing"))
          wind-speed (to-one-decimal (get weather "windSpeed"))
          formatted-location (:formatted-location location)]
      (str temperature temperature-unit " (" apparent-temperature ") " conditions " | Wind " wind-direction " " wind-speed " " wind-speed-unit " | " formatted-location)
      )
    )
  )
