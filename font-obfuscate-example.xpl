<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:c="http://www.w3.org/ns/xproc-step"
		xmlns:tr="http://transpect.io"
  version="1.0">

  <p:output port="result" primary="true"/>  

  <p:option name="file" required="true"/>

  <p:import href="font-obfuscate-declaration.xpl"/>

  <tr:font-obfuscate name="font-obfuscate">
    <p:with-option name="file" select="$file"/>
  </tr:font-obfuscate>

</p:declare-step>
