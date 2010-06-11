declare namespace r="http://code.google.com/p/existdb-contrib";
<html xmlns='http://www.w3.org/1999/xhtml'>
<head><title>Request Information</title></head>
<body>
<h1>Request Information</h1>
<div class='section'>
<h2>Variables Declared</h2>
<table>
<tr><td>A</td><td>=</td><td>{$A}</td></tr>
<tr><td>B</td><td>=</td><td>{$B}</td></tr>
</table>
</div>
<div class='section'>
<h2>Parameters to the Query</h2>
<table>
{ for $name in r:parameter-names()
     return <tr><td>{$name}</td><td>{r:get-parameter($name)}</td></tr>
}
</table>
</div>
</body>
</html>