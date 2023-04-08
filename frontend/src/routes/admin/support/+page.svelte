<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import Error from "$lib/Error.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : "";;
    let userId;
    let name;
    let socket;

    let selectedRoom;

    let myRooms;
    let openRooms;

    let text = "";
    let messages;
    let firstJoin = false;

    onMount(async () => {
        // check admin
        var request = await fetch("http://" + serverAddress + "/account/admingroup", {
            method: "POST",
            body: session
        }).catch(() => {
            goto("/account/login?l=2");
        });
        var result = await request.text();
        if (result !== "true") {
            goto("/account/login?l=2");
        }

        request = await fetch("http://" + serverAddress + "/account/request", {
            method: "POST",
            body: session + ";id;firstName;lastName"
        });
        result = await request.text();
        const data = result.split(";");
        userId = data[0];
        name = data[1] + " " + data[2];
    });

    /**
     * load the rooms the supporter is reliable for
     */
    async function loadMyRooms() {
        myRooms = [];
        var request = await fetch("http://" + serverAddress + "/support/getSupported", {
            method: "POST",
            body: session
        });
        var result = JSON.parse(await request.text());
        for (let i = 0; i < result.length; i++) {
            myRooms[i] = {roomId: result[i].rid, name: result[i].creator.firstName + " " + result[i].creator.lastName};
        }
    }

    /**
     * load all rooms that are not yet supported
     */
    async function loadUnsupportedRooms() {
        openRooms = [];
        var request = await fetch("http://" + serverAddress + "/support/getUnsupported", {
            method: "POST",
            body: session
        });
        var result = JSON.parse(await request.text());
        for (let i = 0; i < result.length; i++) {
            openRooms[i] = {roomId: result[i].rid, name: result[i].creator.firstName + " " + result[i].creator.lastName};
        }
    }

    /**
     * enter a free room that wasn't claimed yet
     * @param roomId the id of the room
     */
    async function joinRoom(roomId) {
        var request = await fetch("http://" + serverAddress + "/support/supportRoom", {
            method: "POST",
            body: JSON.stringify({
                session,
                roomId: roomId
            })
        });
        var result = await request.text();
        if (result === "true") {
            selectRoom(roomId);
            firstJoin = true;
        } else {
            alert("Dem Raum konnte nicht beigetreten werden!");
        }
    }

    /**
     * select a room
     * @param roomId the id of the room
     */
    async function selectRoom(roomId) {
        messages = [];
        selectedRoom = roomId;
        loadMessages();
        connect();
    }

    /**
     * load previous messages
     */
    async function loadMessages() {
        var request = await fetch("http://" + serverAddress + "/support/getMessages", {
            method: "POST",
            body: JSON.stringify({
                session,
                roomId: selectedRoom
            })
        });
        var result = JSON.parse(await request.text());
        for (var i = 0; i < result.length; i++) {
            messages[messages.length] = {userId: result[i].sender.uid, name: result[i].sender.firstName + " " + result[i].sender.lastName, message: result[i].text, type: result[i].type};
        }
    }

    /**
     * leave the room
     */
    async function leaveRoom() {
        loadUnsupportedRooms();
        loadMyRooms();
        selectedRoom = null;
        socket.close();
        messages = [];
    }

    /**
     * close the room
     */
    async function closeRoom() {
        var request = await fetch("http://" + serverAddress + "/support/closeRoom", {
            method: "POST",
            body: JSON.stringify({
                session,
                roomId: selectedRoom
            })
        });
        var result = await request.text();
        if (result === "true") {
            const message = {
                userId,
                name,
                roomId: selectedRoom,
                text: "",
                type: "CLOSE"
            }
            socket.send(JSON.stringify(message));
            logMessage(JSON.stringify(message));
            leaveRoom();
        } else {
            alert("Dem Raum konnte nicht geschlossen werden!");
        }
    }

    /**
     * free the room so other supporters can enter again
     */
    async function freeRoom() {
        var request = await fetch("http://" + serverAddress + "/support/freeRoom", {
            method: "POST",
            body: JSON.stringify({
                session,
                roomId: selectedRoom
            })
        });
        var result = await request.text();
        if (result === "true") {
            const message = {
                userId,
                name,
                roomId: selectedRoom,
                text: name + " hat deinen Raum verlassen. Bitte habe etwas Geduld, bis der nächste Mitarbeiter für dich da ist!",
                type: "FREE"
            }
            socket.send(JSON.stringify(message));
            logMessage(JSON.stringify(message));
            leaveRoom();
        } else {
            alert("Der Raum konnte nicht freigegeben werden!");
        }
    }

    /**
     * connect to the websocket and register the event listeners
     */
    function connect() {
        socket = new WebSocket('ws://' + serverAddress + '/socket');

        // on open
        socket.addEventListener('open', () => {
            if (firstJoin) {
                const message = {
                    userId,
                    name,
                    roomId: selectedRoom,
                    text: name + " ist beigetreten!",
                    type: "JOIN"
                }
                socket.send(JSON.stringify(message));
                logMessage(JSON.stringify(message));
                firstJoin = false;
            }
        });

        // on message
        socket.addEventListener('message', (event) => {
            var data = JSON.parse(event.data);
        
            if (data.roomId == selectedRoom) {
                messages[messages.length] = {userId: data.userId, name: data.name, message: data.text, type: data.type};
            }
        });
    };

    /*
    * send message to backend
    */
    async function sendMessage() {
        if (!text) {
            return;
        }
        const message = {
            userId,
            name,
            roomId: selectedRoom,
            text,
            type: "MESSAGE"
        }
        socket.send(JSON.stringify(message));
        text = "";
        logMessage(JSON.stringify(message));
    }

    /**
     * log a message
     * @param message the message
     */
     async function logMessage(message) {
        await fetch("http://" + serverAddress + "/support/log", {
            method: "POST",
            body: message
        });
    }

    /**
     * scroll to the bottom of a div
     * @param node the div
     */
    const scrollToBottom = node => {
        const scroll = () => node.scroll({
            top: node.scrollHeight,
            behavior: 'smooth',
        });
        scroll();

        return { update: scroll }
    };
</script>

<svelte:head>
    <title>Support-Anfragen - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    <a href="/admin/">Admin-Bereich</a>
    {#if !selectedRoom}
        <div class="rooms">
            <div class="myrooms">
                <h1>Deine Support-Anfragen</h1>
                {#await loadMyRooms()}
                    <LoadingBar />
                {:then} 
                    {#if myRooms.length != 0}
                        {#each myRooms as room}
                            <button on:click={() => selectRoom(room.roomId)}>{room.name}</button>
                        {/each}
                    {:else}
                        <Error error="Du hast keine offenen Support-Räume!" img="/images/messages/success.png" /> <!--https://img.icons8.com/color/256/ok--v1.png-->
                    {/if}
                {:catch}
                    <Error error="Es gab einen Fehler beim Laden der Räume!" />
                {/await}
            </div>
            <div class="openrooms">
                <h1>Offene Support-Anfragen</h1>
                {#await loadUnsupportedRooms()}
                    <LoadingBar />
                {:then} 
                    {#if openRooms.length != 0}
                        {#each openRooms as room}
                            <button on:click={() => joinRoom(room.roomId)}>{room.name}</button>
                        {/each}
                    {:else}
                        <Error error="Derzeit gibt es keine offenen Support-Räume!" img="/images/messages/success.png" /> <!--https://img.icons8.com/color/256/ok--v1.png-->
                    {/if}
                {:catch}
                    <Error error="Es gab einen Fehler beim Laden der Räume!" />
                {/await}
            </div>
        </div>
    {:else}
        <div class="actions">
            <button on:click={leaveRoom}>Zurück</button>
            <button on:click={closeRoom}>Schließen</button>
            <button on:click={freeRoom}>Freigeben</button>
        </div>
        <div class="support">
            <div use:scrollToBottom={messages} class="messages">
                {#each messages as message}
                    {#if (message.type === "JOIN" || message.type === "FREE")}
                        <div class="notice">
                            <p>{message.message}</p>
                        </div>
                    {:else}
                        {#if message.userId == userId}
                            <div class="message own-message">
                                <p>{message.message}</p>
                            </div>
                        {:else}
                            <div class="message other-message">
                                <span>{message.name}</span>
                                <p>{message.message}</p>
                            </div>
                        {/if}
                    {/if}
                {/each}
            </div>
            <div class="send">
                <form action="" on:submit={sendMessage}>
                    <input type="text" bind:value={text} placeholder="Nachricht" maxlength="500" required>
                    <button type="submit"><img src="/images/support/send.png" alt="Send message"></button> <!--https://img.icons8.com/external-flatart-icons-outline-flatarticons/512/external-send-basic-ui-elements-flatart-icons-outline-flatarticons.png-->
                </form>
            </div>
        </div>
    {/if}
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .admin {
        display: flex;
        flex-direction: column;
        margin-left: 5%;
        margin-right: 5%;
        margin-bottom: 4%;
        height: 100%;
        justify-content: center;
    }
    .rooms {
        display: flex;
        justify-content: space-evenly;
        align-items: flex-start;
    }
    .myrooms, .openrooms {
        display: flex;
        flex-direction: column;
    }
    a {
        text-align: center;
        font-size: 42px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
    }
    .rooms button {
        width: 300px;
        margin: 10px;
        padding: 0 15px 0 15px;
        background-color: #fad8af;
        border-radius: 10px;
        font-size: 24px;
        height: 80px;
        color: #3b332b;
    }
    .rooms button:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    .actions {
        display: flex;
        justify-content: center;
    }
    .actions button {
        width: fit-content;
        margin: 10px;
        padding: 5px;
        background-color: #fad8af;
        border-radius: 10px;
        font-size: 24px;
        height: 50px;
        color: #3b332b;
    }
    .actions button:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    .support {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 75%;
        border: 1px solid black;
        border-radius: 10px;
    }
    .messages {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        overflow-y: scroll;
        height: 55vh;
    }
    .message {
        border: 1px solid black;
        border-radius: 10px;
        margin: 5px;
        padding: 2px 5px 2px 5px;
        width: fit-content;
        max-width: 70%;
    }
    .notice {
        margin: 5px;
        padding: 2px 5px 2px 5px;
        width: fit-content;
        max-width: 70%;
        align-self: center;
    }
    .other-message {
        padding: 10px 10px 0 10px;
        background-color: antiquewhite;
    }
    .other-message span {
        color: green;
    }
    .own-message {
        background-color: white;
        align-self: flex-end;
        min-width: 30px;
        text-align: right;
    }
    .send {
        display: flex;
        justify-content: left;
        align-items: center;
        margin: 15px 5px 5px 5px;
    }
    .send button {
        border: 1px solid black;
        border-radius: 10px;
        background-color: white;
        padding: 5px;
        margin: 0;
        margin-left: 5px;
    }
    .send button:hover {
        cursor: pointer;
    }
    .send img {
        width: 38px;
        height: auto;
    }
    form {
        width: 100%;
        display: flex;
        justify-content: left;
    }
    input {
        width: 99%;
        font-size: 18px;
        border: 1px solid black;
        border-radius: 5px;
        top: 0;
        padding: 5px;
    }
    @media (max-width: 640px) {
        .admin {
            margin-bottom: 20%;
        }
        .rooms {
            flex-direction: column;
        }
        .actions {
            flex-direction: column;
        }
        .actions button {
            width: 90%;
        }
        .support {
            margin-top: 10px;
        }
        .messages {
            height: 45vh;
        }
        form {
            justify-content: left;
        }
        .send img {
            width: 32px;
        }
        input {
            width: 79%;
        }
    }
</style>