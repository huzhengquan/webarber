(ns webarber.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [barber.core :as bb]
            [barber.ruse :as ruse]
            [webarber.html :as html]
            [clojure.data.json :as json]
            [clj-leveldb :as ldb]))



(def url-db (ldb/create-db
  (or (System/getProperty "level.url.path")
      "tmp/url_leveldb")
  {:key-decoder byte-streams/to-string :val-decoder byte-streams/to-string}))
(def url-map (atom (into {} (ldb/iterator url-db))))
(println "urlmap count" (count @url-map))
(defn- set-url-map
  [url real-url]
  (swap! url-map assoc url real-url)
  (ldb/put url-db url real-url))

(def page-db (ldb/create-db
  (or (System/getProperty "level.page.path")
      "tmp/page_leveldb")
  {:key-decoder byte-streams/to-string :val-decoder byte-streams/to-string}))
(defn- get-cache [url]
  (if-let [article (ldb/get page-db url)]
    (json/read-str article :key-fn keyword)))
(defn- put-cache [url article]
  (ldb/put page-db url (json/write-str article)))

(def ruse-db (ldb/create-db
  (or (System/getProperty "level.ruse.path")
      "tmp/ruse_leveldb")
  {:key-decoder byte-streams/to-string :val-decoder byte-streams/to-string}))
(defn- put-ruse [domain rematch ruse-map]
  (ruse/put domain rematch ruse-map)
  (ldb/put ruse-db (str domain " " rematch) (pr-str ruse-map)))
(defn- reset-ruse []
  (doseq [[k-str r] (ldb/iterator ruse-db)]
    (let [jr (read-string r)
          [domain rematch] (clojure.string/split k-str #"\s")]
      (ruse/put domain rematch jr))))

(reset-ruse)

(defn- get-article
  [url]
  (println "get-article : ")
  (let [real-url (get @url-map url url)]
    (or (get-cache real-url)
        (if-let [article (bb/url->article real-url)]
          (do
            (println "wget" real-url)
            (if (not (= url (:uri article)))
              (set-url-map url (:uri article)))
            (put-cache (:uri article) article)
            article)))))

(defn- site-home ;get-article 
  [request]
  (let [url (get (:query-params request) "url")]
    (if url
      (html/wrap-html (time (get-article url)) url)
      (html/index-html (:server-name request)))))

(defn- site-admin
  [request]
  (if (contains? (:params request) :delete)
    (do
      (ruse/empty-map)
      (ldb/delete ruse-db (:delete (:params request)))
      (reset-ruse)))
  (html/admin (ldb/iterator ruse-db)))

(defn- admin-post
  [request]
  (let [{domain :domain, rematch :rematch, query :query url :url} (:params request)
        [url-scheme _ url-domain url-path] (clojure.string/split url #"/" 4)]
    (try
      (if (and (= domain url-domain)
               (re-matches (re-pattern rematch) url-path))
        (do
          (put-ruse domain rematch (read-string query))
          "done")
        "error")
      (catch Exception e (str "caught exception: " (.getMessage e))))))

(defroutes app-routes
  (GET "/" request (site-home request))
  (GET "/admin" request (site-admin request))
  (POST "/admin" request (admin-post request))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
