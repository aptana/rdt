 <table bgcolor="#ffffff" border="0" cellpadding="2" cellspacing="5">
   <tr>
    <td align="right" valign="top" width="20"><img src="images/arrow.gif" alt="" border="0" height="16" width="16"></td>
    <td valign="top"><font face="arial,helvetica,geneva" size="-1"><b> Choose a build stream </b>
      <br/>
      Depending on your usage of RDT you can choose between three build streams:
      <ul>
        <li><em>Releases:</em> Regular RDT releases</li>
        <li><em>Integration builds:</em> Builds which are not stable enough to be released but include new features which are worthwhile to be looked at. Release Candidates and Release are published into this stream.</li>
        <li><em>Nightly builds:</em> The latest nightly build, the cutting-edge of RDT development. 
        Usually you do <b>not</b> want to use this. However, it might be useful if you 
		know about certain features or bug fixes which have been done in CVS but are not yet part of the latest release. As common for this kind of build, there is no guarantee of the
		quality of this build. A hint of the quality of this build gives the 
		<a href="http://download.rubypeople.org/nightly/org.rubypeople.rdt.tests.all_.html">result of the (PDE) unit test suite</a>.
        Use it only if you are prepared to face
        troubles and are willing to report problems to the forum or mailing list. If you have created a ruby project with a stable version of RDT before, there might be updates
        necessary in order to open you project. Downgrading a ruby project to a stable version might also be cumbersome.</li>
      </ul>
      </font>
    </td>
  </tr>
  <tr>
  <tr>
    <td align="right" valign="top" width="20"><img src="images/arrow.gif" alt="" border="0" height="16" width="16"></td>
    <td valign="top"><font face="arial,helvetica,geneva" size="-1"><b>Using the Eclipse Update Manager</b>
      <br/>
      <ul>
        <li/>Choose Help<img src="images/arrow_filled.gif" alt="->" border="0">Software Updates<img src="images/arrow_filled.gif" alt="->" border="0">Find and Install
        <li/>In the dialog choose "Search for new features to install" and push next
        <li/>Select "New Remote Site" and add one of the following URL's depending on the build stream you want to follow:
        <ul>
        	<li>http://updatesite.rubypeople.org/release for release builds</li>
        	<li>http://updatesite.rubypeople.org/integration for integration builds including release builds and release candidates</li>
        	<li>http://updatesite.rubypeople.org/nightly for the latest nightly build</li>        	        	
        </ul>
        <li/>Check RDT and push next
        <li/>Select the feature org.rubypeople.rdt and push next. RDT will be installed and be available after restart.
      </ul>
      <b>Automatic updates</b>
      <br/>
		Go to Window<img src="images/arrow_filled.gif" alt="->" border="0">Preferences...<img src="images/arrow_filled.gif" alt="->" border="0">Install/Update<img src="images/arrow_filled.gif" alt="->" border="0">Automatic Updates
        and check to automatically find new updates.<br/>
        There are restrictions for automatic updates: not every update will be handeled automatically. E.g a major release update (e.g. from 1.0.0 to 2.0.0) can not be done automatically.
        In order to have service releases handled (e.g. from 0.6.0 to 0.7.0) go to 
        Window<img src="images/arrow_filled.gif" alt="->" border="0">Preferences...<img src="images/arrow_filled.gif" alt="->" border="0">Install/Update
        and set valid updates to 'compatible'.
      </font>
      
    </td>
  </tr>
  <tr>
    <td align="right" valign="top" width="20"><img src="images/arrow.gif" alt="" border="0" height="16" width="16"></td>
    <td valign="top"><font face="arial,helvetica,geneva" size="-1"><b>Download the zipped plugin</b>
      <br/>
      <p>It is recommended to use the update manager for installing RDT. The alternative is to download the zip file and extract it into the eclipse installation directory.      
      The zip files of integration and release builds are available at Sourceforge.net's <a href="http://sourceforge.net/project/showfiles.php?group_id=50233">RDT Files</a> section.<br/>
      Past releases are available here as well.</p>
      <p>Download the nightly build zip from the <a href="http://download.rubypeople.org/nightly">nightly build site</a>.</p>
      </font>
    </td>
  </tr>  
</table>