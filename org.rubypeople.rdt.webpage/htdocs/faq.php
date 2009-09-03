
    <table bgcolor="#ffffff" border="0" cellpadding="2" cellspacing="5" >
      <tr>
      <td align="right" valign="top" width="20"><img src="images/arrow.gif" alt="->" border="0" height="16" width="16"></td>
      <td valign="top"><font face="arial,helvetica,geneva" size="-1"><b> Why aren't my output statements appearing?</b>
        <br/>
<p>If a user runs code such as:<br/>
<pre>puts "gimme your name:"
name = gets.chomp
puts "hello, #{name}"</pre>
You won't see the "gimme your name", before being prompted for 
input (name = gets.chomp) first. After the input, the two puts lines 
are printed out. This is a result of the way streams are handled. <br/>
There's a couple solutions. The first is to manually flush the stream after each put. The second is to set:<br/>
<pre>$stdout.sync = true</pre>
</p>
        </font>
      </td>
    </tr>
      <tr>
      <td align="right" valign="top" width="20"><img src="images/arrow.gif" alt="->" border="0" height="16" width="16"></td>
      <td valign="top"><font face="arial,helvetica,geneva" size="-1"><b> What language is RDT written in?</b>
        <br/>
<p>RDT is written almost entirely in Java. The Eclipse platform is Java based, and so
writing plugins for it requires us to use Java. (Unless you're a smart cookie who can find
a way to let us use ruby - perhaps JRuby?)<br/>
<br/>
That being said, we do have some ruby scripts included in RDT to help us interface with your ruby code.
 A good example of this is the Test::Unit runner. The plugin actually communicates over a socket from 
 a Java class to a ruby script. The ruby script handles the setup, running the test case and informing the Java runner about the status.
</p>
        </font>
      </td>
    </tr>    
    <tr>
      <td align="right" valign="top" width="20"><img src="images/arrow.gif" alt="->" border="0" height="16" width="16"></td>
      <td valign="top"><font face="arial,helvetica,geneva" size="-1"><b> Which version of Eclipse should I install?</b>
        <br/>
<p>The RDT Development Team always aims to be compatible with the absolute latest version of Eclipse.
 The current release of RDT should be compatible with Eclipse 3.0+.<br/></p>
        </font>
      </td>
    </tr>
        <tr>
      <td align="right" valign="top" width="20"><img src="images/arrow.gif" alt="->" border="0" height="16" width="16"></td>
      <td valign="top"><font face="arial,helvetica,geneva" size="-1"><b> Which components of Eclipse do I need?</b>
        <br/>
<p>Generally speaking you should be able to download the Eclipse platform, without the JDT (unless you plan on doing Java development).<br/>
The download for Eclipse which includes only the platform (minus the plugin SDK and JDT) is typically labeled "Platform Runtime Binary".
This the bare minimum you need to be able to run RDT. Obviously anyone with the full version including the SDK, or a version with the
platform plus JDT should be able to RDT as well.
</p>

</p>
        </font>
      </td>
    </tr>   
    </table>
