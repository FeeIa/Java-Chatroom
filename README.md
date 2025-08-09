# Chatroom - CSC1004 Project 1

There are three types of features implemented:
- **Basic**: Features required by the course project details.
- **Advanced**: Features that are implemented for extra score.
- **Additional**: Features that are implemented for self-interest.

## Features
### Basic Features
- **Multithreading Implementation**: The program can run multiple command-line interfaces on a single workstation. Closing/opening a new interface does not affect the current ones.
- **User Identification**: Users are identified by the `name#id` format. The program will ask for a name, while the ID is an auto-assigned 5-digit number.
- **Public Messaging**: Users can send messages via keyboard input and view other users' messages publicly.
- **Currently Connected Users**: New users entering a room will be provided with the name and ID of other users currently connected to the room. Alternatively, users can use the command `/users-list` at runtime.
- **Users Leaving**: Users will be notified when a user leaves the room.
- **Real-Time Indicator**: Displays the current time when a user sends or receives a message.

### Advanced Features
- **Receiver Display**: Users can check the receivers of the specified message using the `/print-receivers message` command.
- **Chat Storage**: Upon joining, Users will access the chat history immediately. Chat history is stored server-side, not in a local file. Termination of the running server will delete the chat history.
- **Search Function**: Users can search the chat log by keyword using the `/search keyword` command. A list of all messages with the given keyword will be displayed.

### Additional Features:
- **Private Messaging**: Users can send a direct (private) message to a specified user using the `/msg name#id message` command.
- **Multiple Servers**: You can specify the program to start a server or connect a client on different ports.

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
After starting the client and connecting successfully, users will be prompted to enter a name. The server will automatically assign a unique 5-digit ID.

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
- `/current-server` - Gives the information of the current server port.
- `/users-list` - Shows all other connected users.
- `/msg name#id message` - Sends a private message to a specific user.
- `/search keyword` - Searches for all messages or users by the specified keyword.
- `/print-receivers message` - Prints all receivers of the specified message.
