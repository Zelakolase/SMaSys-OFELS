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
الادارة</h2>
</center>
<body style="background-color:rgba(192,192,192,0.5);">
<div class="topnav" id="myTopnav">
  <a href="#RAMStart">اخلاء الذاكرة</a>
  <a href="#Add">اضافة مستخدم</a>
  <a href="#Delete">حذف مستخدم</a>
  <a href="#Log">السجل</a>
  <a href="#View">قائمة المستخدمين</a>
  <div id="Logout">
<form action="/" method="post">
    <center> <button name="logout" value="1" class="big_btn" style="background-color: red;" type="submit">تسجيل خروج</button></center> 
</form>
</div>
  <a href="javascript:void(0);" class="icon" onclick="myFunction()">
    <i class="fa fa-bars"></i>
  </a>
</div>
<br><br><br><br><br>

</div>

<div id="RAMStart">
<center><h3 style="color: #160042;">اخلاء ذاكرة الوصول العشوائي</h3></center>
<form action="/" method="post">
    <center> <button name="gc" value="1" class="big_btn" style="background-color: red;" type="submit">اخلاء</button></center> 
</form></div>

<div id="Add">
<center><h3 style="color: #160042;">اضافة مستخدم جديد</h3></center>
<form action="/" method="post">  
        <div class="container">   
                    <input type="hidden" id="add" name="add" value="true"> 
            <label>اسم المستخدم</label>   
            <input style="width: 30%;" type="username" name="username" required><br>
            <label>الأسم الكامل</label>   
            <input style="width: 30%;" type="name" name="name" required><br>
            <label>كلمة السر</label>   
            <input style="width: 30%;" type="password" name="password" required><br>
            <br>
            <center> <button type="submit">اضافة</button> </center> 
        </div>   
    </form>   
    </div>

<div id="Delete">
<center><h3 style="color: #160042;">حذف مستخدم</h3></center>

    <form action="/" method="post">  
        <div class="container">   
            <label>اسم المستخدم</label>
            <input type="hidden" id="delete" name="delete" value="true"> 
            <input style="width: 20%;" type="text" name="username" required>
            <br>
            <center> <button type="submit">حذف</button> </center> 
        </div>   
    </form>
</div>

<div id="Log">
<center><h3 style="color: #160042;">السجل</h3></center>
<form action="/" method="post">
    <center> <button name="log" value="1" class="big_btn" style="background-color: red;" type="submit">عرض</button></center> 
</form>
</div>
<div id="View">
<form action="/" method="post">
    <center> <button name="table" value="1" class="big_btn" style="background-color: red;" type="submit">عرض المستخدمين</button></center> 
</form>
</div>
</body>
</html>