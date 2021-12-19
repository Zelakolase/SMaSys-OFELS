<html>
<head>
<link rel="stylesheet" href="data/style.css">
</head>
<center><h2 style="color: #160042;">
الملفات</h2></center>
<center><button class="big_btn" onClick="window.location.href='/';" type="submit">القائمة</button></center> 

<body style="background-color:rgba(192,192,192,0.5);">
  <center><h3>رفع ملف جديد</h3></center>
  <form method="post" action="/upload" enctype="multipart/form-data">  
        <div class="container">   
            <label>الملف</label>   
            <input style="width: 30%;" type="file" name="filename" required>
            <br>
            <center> <button type="submit">رفع</button> </center> 
        </div>   
    </form>     
    <center><h3>أو جلب ملف</h3></center>
    <form method="post">  
        <div class="container">   
            <label>كود الملف  </label>
            <input style="width: 20%;" type="text" name="id" required>
            <br>
            <center> <button type="submit">جلب</button> </center> 
        </div>   
    </form>
    <center>     
    
    <br><br>
<form action="/" method="post">
    <center> <button name="logout" value="1" class="big_btn" style="background-color: red;" type="submit">تسجيل خروج</button></center> 
</form></body>
</html>