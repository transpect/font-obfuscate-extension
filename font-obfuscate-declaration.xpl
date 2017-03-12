<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step 
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:tr="http://transpect.io"
  name="font-obfuscate"
  type="tr:font-obfuscate"
  version="1.0">

  <p:output port="result" primary="true"/>
  
  <p:option name="file" required="true"/>

</p:declare-step>
