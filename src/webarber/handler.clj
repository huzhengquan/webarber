(ns webarber.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [barber.core :as bb]
            [webarber.html :as html]
            [clojure.data.json :as json]
            [clj-leveldb :as ldb]))



(def db (ldb/create-db
  (or (System/getProperty "level.url.path")
      "tmp_url_leveldb")
  {:key-decoder byte-streams/to-string :val-decoder byte-streams/to-string}))
(def url-map (atom (into {} (ldb/iterator db))))
(println "urlmap count" (count @url-map))
(defn- set-url-map
  [url real-url]
  (swap! url-map assoc url real-url)
  (ldb/put db url real-url))

(def page-db (ldb/create-db
  (or (System/getProperty "level.page.path")
      "tmp_page_leveldb")
  {:key-decoder byte-streams/to-string :val-decoder byte-streams/to-string}))
(defn- get-cache [url]
  (if-let [article (ldb/get page-db url)]
    (json/read-str article :key-fn keyword)))
(defn- put-cache [url article]
  (ldb/put page-db url (json/write-str article)))

(defn get-article
  [url]
  (let [real-url (get @url-map url url)]
    (or (get-cache real-url)
        (if-let [article (bb/url->article real-url)]
          (do
            (println "wget" real-url)
            (if (not (= url (:uri article)))
              (set-url-map url (:uri article)))
            (put-cache (:uri article) article)
            article)))))

(defn site-home ;get-article 
  [request]
  (let [url (get (:query-params request) "url")]
    (if url
      (html/wrap-html (time (get-article url)) url)
      (html/index-html (:server-name request)))))

(defroutes app-routes
  (GET "/" request (site-home request))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
