package ru.otus.java.basic.april.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.april.server.HttpRequest;
import ru.otus.java.basic.april.server.app.Item;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GetItemsRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        List<Item> items = Arrays.asList(
                new Item(1L,"Bread",50),
                new Item(2L,"Milk",150),
                new Item(3L,"Cheese",400)
        );
        String result = new Gson().toJson(items);

        String response = """
HTTP/1.1 200 OK
Content-Type: text/html

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Items</title>
<style>
body {margin:0;padding:0;font-family:sans-serif;height:100vh;display:flex;justify-content:center;align-items:center;background:linear-gradient(135deg,#020617,#1e293b);color:white;}
.container {perspective:1200px;}
.card {width:360px;padding:30px;border-radius:20px;background:rgba(255,255,255,0.05);backdrop-filter:blur(20px);box-shadow:0 20px 40px rgba(0,0,0,0.5);text-align:center;transform-style:preserve-3d;transition:transform 0.2s ease;position:relative;}
.glow {position:absolute;width:200px;height:200px;background:radial-gradient(circle, rgba(99,102,241,0.5), transparent 70%);top:-50px;right:-50px;filter:blur(40px);}
pre {text-align:left;background:rgba(30,41,59,0.8);padding:20px;border-radius:10px;overflow:auto;}
a.button {display:block;margin-top:10px;padding:10px;border-radius:10px;background:linear-gradient(135deg,#38bdf8,#6366f1);color:white;text-decoration:none;font-weight:bold;transition:transform 0.2s,box-shadow 0.2s;}
a.button:hover {transform:translateY(-3px);box-shadow:0 15px 25px rgba(0,0,0,0.4);}
</style>
</head>
<body>
<div class="container">
<div class="card" id="card">
<div class="glow"></div>
<h1>Items</h1>
<pre>""" + result + """
</pre>
<a class="button" href="/">Back Home</a>
</div>
</div>
<script>
const card=document.getElementById('card');
document.addEventListener('mousemove', e=>{const x=(window.innerWidth/2-e.pageX)/25;const y=(window.innerHeight/2-e.pageY)/25;card.style.transform=`rotateY(${x}deg) rotateX(${y}deg)`});
document.addEventListener('mouseleave', ()=> card.style.transform='rotateY(0deg) rotateX(0deg)');
</script>
</body>
</html>
""";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}