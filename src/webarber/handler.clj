(ns webarber.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [barber.core :as bb])
  (:import [java.net URLEncoder]))

(def pug (atom []))

(defn index-html []
(str
"<html>
<head>
<title>网页理发师</title>
<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1' />
<style type='text/css'>ul,li{padding:0;margin:0;list-style:none}</style>
</head>
<body style='text-align:center;background-color:#e5e4db'>
  <div style='margin:50px 0px'><img src='/images/logo.png' /></div>
  <p style='color:#666;font-size:12px'>剔除网页上不重要的元素，只保留干净的正文部分。请输入网址回车:</p>
  <form onsubmit='javascript:if(document.getElementsByName(\"url\")[0].value.split(\"/\").length<5){alert(\"本东东只支持有大段文字的页面，如新闻内容页、博客内容页\");return false}'><input name=\"url\" style='min-width:20px;width:90%;max-width:35em; line-height:150%; font-size:1.2em; padding:2px 5px' value='http://' /></form><br><ul>"
(apply str
  (let [url-set (set (for [i @pug] (first i)))
        title-map (into {} @pug)]
     (for [i url-set] (str "<li><a href='/?url=" (URLEncoder/encode i) "'>" (get title-map i i) "</a></li>"))))
"</ul></body>
</html>"
))


(defn wrap-html
  [article url]
  (if (and article (:html article))
    (do
      (reset! pug (take 20 (conj @pug [url (:title article)])))
      (str
      "<html>
      <head>
      <title>"
      (:title article)
      "</title>
      <meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1' />
      <style type='text/css'>
      div#nav a{color:yellow}
      </style>
      </head>
      <body style='padding:0;margin:0px;background-color:#e5e4db'>
      <div id='nav' style='background-color:#000;color:white'><a href=\"/\">首页</a>
      <a href='" url "' target=_blank>原网页</a>
      <span>准确分:" (:weight article) "</span>
      </div>
      <div style='padding:1em; max-width:44em; margin:20px auto; line-height:2em;color:#333;background-color:#f6f4ec;border-radius: 5px;'>"
      "<h1>" (:title article) "</h1>"
      (:html article)
      "</div>
       </body>
       </html>"))
    (str
      "<html>
       <head>
       <meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1' />
       </head>
       <body style='text-align:center;margin:50px 0px'>分数:" (:weight article)
       "<div>解析结果不适合阅读，建议查询<a href='"
       url
       "'>原网页</a></div><a href='/'>返回首页重新尝试其他网页</a></body></html>")))

(defn get-article 
  [request]
  (let [url (get (:query-params request) "url")]
    (if url
      (wrap-html (bb/url->article url) url)
      (index-html))))

(defroutes app-routes
  (GET "/" request (get-article request))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
