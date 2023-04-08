<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import Error from "$lib/Error.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import { browser } from "$app/environment";
    import { serverAddress, checkAuth } from "$lib/stores.js";
    import { goto } from "$app/navigation";

    let session;
    let text = "";
    let socket;

    let userId;
    let name;

    let hasRoom = false;
    let roomId;
    let closed = false;

    let messages = [];

    /**
     * prepare the support
     */
    async function prepareSupport() {
        session = browser ? window.sessionStorage.getItem("session") ?? "" : "";
        let auth = await checkAuth();
        if (!auth) {
            goto("/account/login?l=2");
            return;
        }
        await getData();

        var request = await fetch("http://" + serverAddress + "/support/hasRoom", {
            method: "POST",
            body: session
        });
        var result = await request.text();
        if (result == 0) {
            return;
        }
        hasRoom = true;
        roomId = result;
        loadMessages();
        connect();
    }

    /**
     * load the data of the user
     */
    async function getData() {
        var request = await fetch("http://" + serverAddress + "/account/request", {
            method: "POST",
            body: session + ";id;firstName;lastName"
        });
        var result = await request.text();
        const data = result.split(";");
        userId = data[0];
        name = data[1] + " " + data[2];
    }

    /**
     * load previous messages
     */
    async function loadMessages() {
        var request = await fetch("http://" + serverAddress + "/support/getMessages", {
            method: "POST",
            body: JSON.stringify({
                session,
                roomId
            })
        });
        var result = JSON.parse(await request.text());
        for (var i = 0; i < result.length; i++) {
            messages[messages.length] = {userId: result[i].sender.uid, name: result[i].sender.firstName + " " + result[i].sender.lastName, message: result[i].text, type: result[i].type};
        }
    }

    /**
     * create a room
     */
    async function createRoom() {
        var request = await fetch("http://" + serverAddress + "/support/createRoom", {
            method: "POST",
            body: session
        });
        var result = await request.text();
        if (result == 0) {
            return;
        }
        hasRoom = true;
        roomId = result;
        connect();
    }

    /**
     * connect to the WebSocket
     */
    function connect() {
        socket = new WebSocket('ws://' + serverAddress + '/socket');

        socket.addEventListener('message', (event) => {
            var data = JSON.parse(event.data);
        
            if (data.roomId == roomId) {
                if (data.type === "CLOSE") {
                    closed = true;
                    alert(data.name + " hat deinen Raum geschlossen!");
                    return;
                }
                messages[messages.length] = {userId: data.userId, name: data.name, message: data.text, type: data.type};
            }
        });
    }

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
            roomId,
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
    <title>Support - DogNeeds</title>
</svelte:head>

<Header />
<div class="page">
    <h1>Support-Chat</h1>
    {#await prepareSupport()}
        <LoadingBar />
    {:then} 
        <div class="support">
            {#if hasRoom}
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
                        <input disabled="{closed}" type="text" bind:value={text} placeholder="Nachricht" maxlength="500" required>
                        <button disabled="{closed}" type="submit"><img src="/images/support/send.png" alt="Send message"></button> <!--https://img.icons8.com/external-flatart-icons-outline-flatarticons/512/external-send-basic-ui-elements-flatart-icons-outline-flatarticons.png-->
                    </form>
                </div>
            {:else}
                <div class="error">
                    <Error error="Du musst zuerst einen Raum erstellen!" />
                    <button on:click={createRoom}>Raum erstellen</button>
                </div>
            {/if}
        </div>
    {/await}
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .page {
        margin-left: 5%;
        margin-right: 5%;
        height: 100%;
    }
    .support {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
        border: 1px solid black;
        border-radius: 10px;
    }
    .error {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
        justify-content: center;
        align-items: center;
        padding: 5px;
    }
    .error button {
        border: 1px solid black;
        border-radius: 10px;
        background-color: white;
        margin: 2px;
        padding: 10px;
        font-size: 22px;
    }
    .error button:hover {
        background-color: bisque;
        cursor: pointer;
    }
    .messages {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        overflow-y: auto;
        height: 60vh;
    }
    .message {
        border: 1px solid black;
        border-radius: 10px;
        margin: 10px;
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
    .send button:disabled {
        cursor: not-allowed;
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
    input:disabled {
        cursor: not-allowed;
    }
    @media (max-width: 640px) {
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