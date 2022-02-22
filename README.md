# ChatApp
Chat app made in Java and JavaFX using sockets and MySQL. This is a server-client application, so multiple instances of the client should be running at the same.

## Features
* Peer to peer messaging 
* Offline messaging (the message sent to an offline user, will be loaded when the chat is opened)
* Persistence implemented from saving the chats
* Login/register with a username
* Ability to add friends and create conversations with them.
* User-friendly warnings and alerts to guide the user around the app.

![Screenshot 2022-02-21 at 19 45 02](https://user-images.githubusercontent.com/100039479/155013270-b2836c24-fcc8-4c55-9e6a-93606533fa07.jpg)
![Screenshot 2022-02-21 at 19 45 09](https://user-images.githubusercontent.com/100039479/155013279-506405f9-ed99-4c35-a19d-de438f3df6f7.jpg)
![Screenshot 2022-02-21 at 19 45 19](https://user-images.githubusercontent.com/100039479/155013296-1c38ec3f-4ac1-47ee-99f2-62d108910fe0.jpg)
![Screenshot 2022-02-21 at 19 45 29](https://user-images.githubusercontent.com/100039479/155013309-4b9f2a09-beff-4acc-bf5b-7bc154c0c23d.jpg)
![image](https://user-images.githubusercontent.com/100039479/155013332-7153f39b-a49f-484b-beee-748f0371a370.jpeg)

## Getting started![image](https://user-images.githubusercontent.com/100039479/155145024-cbaf1421-04d2-4499-b6d8-04e63c75d515.jpeg)![image](https://user-images.githubusercontent.com/100039479/155145035-311edff9-209d-433c-89c1-4aade9661b86.jpeg)

To run the file you should connect the database in the repository to the code, using the MySQL connector.
Login in the app with a username (for each client) found in the database or register with a new one. 

* To message someone, you should add for both the sender client and the recipient client the usernames to the friend list and create a chat with the other on each end.
