(ns webarber.html
  (:import [java.net URLEncoder]))

(def pug (atom []))

(defn index-html [server-name]
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
  <form onsubmit='javascript:if(document.getElementsByName(\"url\")[0].value.split(\"/\").length<4){alert(\"异常的网址\");return false}'><input name=\"url\" style='min-width:20px;width:90%;max-width:35em; line-height:150%; font-size:1.2em; padding:2px 5px' value='http://' /></form><br><ul>"
(apply str
  (let [url-set (set (for [i @pug] (first i)))
        title-map (into {} @pug)]
     (for [i url-set] (str "<li><a href='/?url=" (URLEncoder/encode i) "'>" (get title-map i i) "</a></li>"))))
"</ul>
<br>
<div><a href=\"javascript:window.location.href='http://" server-name "/?url='+encodeURI(window.location.href);\">barber</a> &lt;--右键点击链接并加入书签后在访问网页时可以随时切换
</div>
</body>
</html>"
))


(defn wrap-html
  [article url]
  (if (and article (:html article))
    (do
      (reset! pug (take 100 (conj @pug [(:uri article) (:title article)])))
      (str
      "<html>
      <head>
      <title>" (:title article) "</title>
      <meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1' />
      <style type='text/css'> div#nav a{color:yellow} </style>
      </head>
      <body style='padding:0;margin:0px;background-color:#e5e4db'>
      <div id='nav' style='background-color:#000;color:white'><a href=\"/\">首页</a>
      <a href='" (:uri article) "' target=_blank>原网页</a>
      </div>
      <div style='padding:1em; max-width:44em; margin:20px auto; line-height:2em;color:#333;background-color:#f6f4ec;border-radius: 5px;'>"
      "<h1>" (:title article) "</h1>"
      (:html article)
      "</div></body></html>"))
    (str
      "<html>
       <head> <meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1' /> </head>
       <body style='text-align:center;margin:50px 0px'>"
       "<div>解析结果不适合阅读，建议查询<a href='"
       url
       "'>原网页</a></div><a href='/'>返回首页重新尝试其他网页</a></body></html>")))


