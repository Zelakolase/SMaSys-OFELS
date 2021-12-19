<html>
<head>
<link rel="stylesheet" href="data/style.css">
</head>
<style>
.topnav {
  overflow: hidden;
}

.topnav a {
  float: right;
  display: block;
  color: #160042;
  text-align: center;
  padding: 14px 16px;
  text-decoration: none;
  font-size: 17px;
}

.topnav a:hover {
  background-color: #ddd;
  color: black;
}

.topnav a.active {
  background-color: #04AA6D;
  color: white;
}

.topnav .icon {
  display: none;
}

@media screen and (max-width: 600px) {
  .topnav a:not(:first-child) {display: none;}
  .topnav a.icon {
    float: right;
    display: block;
  }
}

@media screen and (max-width: 600px) {
  .topnav.responsive {position: relative;}
  .topnav.responsive .icon {
    position: absolute;
    right: 0;
    top: 0;
  }
  .topnav.responsive a {
    float: none;
    display: block;
    text-align: right;
  }
}
</style>
<center><h2 style="color: #160042;">
البريد الألكتروني</h2>
</center>
<body style="background-color:rgba(192,192,192,0.5);">
<center><button class="big_btn" onClick="window.location.href='/';" type="submit">القائمة</button></center> 

<div class="topnav" id="myTopnav">
  <a href="#Recieved" >الواردة</a>
  <a href="#Sent">المرسلة</a>
  <a href="#Compose">انشاء</a>
</div>
<br><br><br><br><br>
<center><h4 style="color: #160042;">
الواردة</h4>
<div class="Received">[[InboxTable]]</div>
<center><h4 style="color: #160042;">
الصادرة</h4>
<div class="Sent">[[OutboxTable]]</div>
</center>

<div class="Compose">
<form action="/mail" method="post">  
        <div class="container">   
            <label>الى </label>   
            <input type="text" name="to" required>
            <br>
            <label>عنوان</label>   
            <input type="text" name="subject" required>  
            <br>
            <label>محتوى</label>   
            <textarea type="text" name="content" required ></textarea>  
            <br>
            <center> <button type="submit">ارسال</button> </center> 
        </div>   
    </form>  
</div>
</body>

</html>