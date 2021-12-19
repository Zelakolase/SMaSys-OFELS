<html>
<head>
<link rel="stylesheet" href="data/style.css">
</head>
<center><h2 style="color: #160042;">
المعلومات الشخصية</h2>
</center>
<body class="" style="background-color:rgba(192,192,192,0.5);">
<div>
<center><button class="big_btn" onClick="window.location.href='/';" type="submit">القائمة</button></center> 
<h3 style="color: #160042;">الأسم الكامل : [[Full-Name]]</h3>
<h3 style="color: #160042;">أسم المستخدم : [[User-Name]]</h3>
<br><br>
<center>
<form action="/personal" method="post">
<label>كلمة المرور القديمة</label>
<input style="width: 20%;" type="password" name="oldpwd" required>

<label>كلمة المرور الجديدة</label>
<input style="width: 20%;" type="password" name="newpwd" required>

<label>تأكيد كلمة المرور الجديدة</label>
<input style="width: 20%;" type="password" name="confirmpwd" required>

  <button class="big_btn" style="background-color: red;" type="submit">تغيير كلمة المرور</button>
 </form>
  <form action="/" method="post">
    <center> <button name="logout" value="1" class="big_btn" style="background-color: red;" type="submit">تسجيل خروج</button></center> 
</form>
</center>
</div>
</body>
</html>