Useful commands for the restful server:

To build to container:
mvn clean package
sudo docker build -t azustor .

To test the container:
sudo docker run --name azustor --rm -it -v /var/tmp/voltest:/data -p 3001:8080 azustor
