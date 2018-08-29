# auxledger-zortag-integration
Zortag integration with Auxledger for distributed supply chain tracking

<b>Prerequisites:</b>
<br>1. You need ZorTag 3D Label and access to Zortag API. To get these Contact <a href="https://www.zortag.com" target="_blank">ZorTag</a>
<br>2. Android Studio 3.0 or higher with Kotlin Runtime


<b>Follow the instructions below to build and run</b>

1. Clone repository on your local machine
<br><code>git clone https://github.com/auxesisgroup/auxledger-zortag-integration.git</code>

2. Open in Android Studio

3. Create local.properties file inside the <b>auxledger-zortag-integration</b> directory with following properties.
<br><code>sdk.dir=/your/local/Android/Sdk/path</code>
<br><code>zortagHeader="Authorixation: ZorKey your-api-key"</code>
<br><code>zortagUrl="put zortag api url here"</code>
<br><code>auxledgerNodeUrl="put your ethereum blockchain node endpoint here"</code>
<br><code>privateKey="put private key of your wallet account here. It should be of 64 characters."</code>

4. Build and install on your android device using <b>Run 'app' or Shift+F10</b> in Android Studio.

