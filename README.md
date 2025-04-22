# Chatroom - CSC1004 Project 1
### Developed by 124040016 - Bryan Edelson

This is one of the three projects in **CSC1004** course of CUHK-SZ. There are three types of feature implemented:
- **Basic**: Features that are required by the course project details.
- **Advanced**: Features that are implemented for extra score.
- **Additional**: Features that are implemented for self-interest.

## Features
### Basic Features
- **Multithreading Implementation**: The program can run multiple command-line interfaces in a single workstation. Closing/opening new interface does not affect current ones.
- **User Identification**: Users are identified by the `name#id` format. The program will ask for a name while id is an auto-assigned 5 digits number.
- **Public Messaging**: Users are able to send message via keyboard input and view other users message publicly.
- **Currently Connected Users**: New users entering a room will be provided with the name and id of other users currently connected to the room. Alternatively, users can use the command `/users-list` in runtime.
- **Users Leaving**: Users will be notified when a user leaves the room.
- **Real-Time Indicator**: Displays the current time when a user sends or receivers a message.

### Advanced Features
- **Receiver Display**: Users can check the receivers of specified message using the `/print-receivers message` command.
- **Chat Storage**: Users will get access to the chat history immediately upon joining. Chat history is stored server side, not in a local file. Termination of the running server will delete the chat history.
- **Search Function**: Users can search the chat log by keyword using the `/search keyword` command. A list of all messages with given keyword will be displayed.

### Additional Features:
- **Private Messaging**: Users are able to send a direct (private) message to a specified user using the `/msg name#id message` command.
- **Multiple Servers**: You can specify the program to start a server or connect a client in different ports.

## How To Run The Program
You can skip the compilation step if you are using an IDE. Make sure to **allow multiple instances** to run.

There are **two ways** to run the program:

### Running Each Code Individually:
1. **Compile the Code**: It is recommended to compile the code inside the chatroom folder.
```
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

2. **Run the Server**: You will be prompted to enter a port to connect to.
```
java -cp out chatroom.server.Server
```

3. **Run the Client**: You will be prompted to enter a port to connect to.
```
java -cp out chatroom.client.Client
```

### Running the Main File:
1. **Compile the Code**: It is recommended to compile the code inside the chatroom folder.
```
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

2. **Run the Main File**:
```
java -cp out chatroom.Main
```
3. **Start as Server or Client**: The program will ask you to start as a Server or Client. You will be prompted to enter a port to connect to.

## Overview and Usage
### Connecting to Server
After starting the client and connected successfully, users will be prompted to enter a name. The server will automatically assign a unique 5-digits ID.

### Sending Messages
- **Public Message:** Simply type the message and hit enter to send it to everyone in the chatroom.
- **Private Message:** Use the `/msg` command to send a private message.
  ```
  /msg name#id message
  ```
  **Example:**
  ```
  /msg SomeRandomNameHere#12345 some random message here
  ```
  ```
  /msg Some Spaced Name Here#12345 hello
  ```

### Commands
Please note that commands are not case-sensitive. Type `/commands` to view all available commands.
- `/current-server` - Gives the information of current server port.
- `/users-list` - Shows all other connected users.
- `/msg name#id message` - Sends a private message to a specific user.
- `/search keyword` - Searches for all messages or users by specified keyword.
- `/print-receivers message` - Prints all receivers of the specified message.