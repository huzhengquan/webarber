(defproject webarber "0.1.3"
  :description "基于barber,为了改善文章阅读体验"
  :url "http://bb.unclose.org"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [factual/clj-leveldb "0.1.0"]
                 [org.clojure/data.json "0.2.5"]
                 [barber "0.1.7"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler webarber.handler/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}
  :jvm-opts ["-Dlevel.url.path=db/url"
             "-Dlevel.ruse.path=db/ruse"
             "-Dlevel.page.path=db/page"])
