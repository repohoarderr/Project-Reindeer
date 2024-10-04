# Project-Reindeer

## Installation:
### Configure Tomcat
1. Clone the repo
2. Create a war artifact
3. Download and install [Tomcat 10.1.x](https://tomcat.apache.org/download-10.cgi)
4. Move war archive to `./{tomcat location}/webapps/`
5. Run Tomcat
6. Go to the webpage [localhost:8080/elk_war/api/hello-world](http://localhost:8080/elk_war/api/hello-world) to test. You should see "hello".
### Configure React
1. Download [Node.js](https://nodejs.org/en/download/prebuilt-installer)
2. Walk through the installation, accept the defaults.
3. Download zip from frontend-update-prod.
4. Unpack the archive.
5. Open a terminal window.
6. Navigate to the directory the archive produced.
7. Run `npm i` to download all the necessary node packages.
8. Run "npm run client:dev". Wait for it to finish.
9. Run "npm run server:dev"(Open a new terminal window if needed). The webpage should open.
10. If a webpage does not automatically open, go to [localhost:5678](http://localhost:5678)
