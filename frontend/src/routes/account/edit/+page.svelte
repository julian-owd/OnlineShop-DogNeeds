<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import EditAccountItem from "$lib/account/edit/EditAccountItem.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import { serverAddress, checkAuth } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";
    import { page } from "$app/stores";

    const successColor = "#9de683";
    const errorColor = "#fa6969";

    // info / error message field
    let infoDisplay = "none";
    let backgroundColor = successColor;
    let messageText = [
        ""
    ];

    let session;

    let auth = false;
    let password;

    let userDetails = {};
    let addressDetails = {};
    let popupWindow;
    let windowDisplay = "none";
    let windowType = "";

    onMount(() => {
        const resultId = $page.url.searchParams.get("r");
        if (resultId == 1) {
            // normal logout
            infoDisplay = "grid";
            backgroundColor = successColor;
            messageText[0] = "Deine Änderungen wurden übernommen.";
        } else if (resultId == 2) {
            // error logout
            infoDisplay = "grid";
            backgroundColor = errorColor;
            messageText[0] = "Dein Account konnte nicht gelöscht werden!";
        }
    });

    /**
     * load the data of the user
     */
    async function loadDetails() {
        let auth = await checkAuth();
        if (!auth) {
            goto("/account/login?l=2");
            return;
        }

        session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

        let request = await fetch("http://" + serverAddress + "/account/request", {
            method: "POST",
            body: session + ";id"
        });
        let userId = await request.text();
        request = await fetch("http://" + serverAddress + "/account/get", {
            method: "POST",
            body: JSON.stringify({
                session,
                userId
            })
        });
        let result = JSON.parse(await request.text());
        userDetails.email = result.email;
        userDetails.password = result.password;
        userDetails.firstName = result.firstName;
        userDetails.lastName = result.lastName;
        addressDetails.plz = result.aid.plz;
        addressDetails.city = result.aid.city;
        addressDetails.street = result.aid.street;
        addressDetails.number = result.aid.number;
    }

    /**
     * show the popup window to edit details
     * 
     * @param type which detail should the window be for
     */
    async function showWindow(type) {
        //hideWindow();
        if (!type) {
            return;
        }
        popupWindow = (await import("$lib/account/edit/EditAccountWindow.svelte")).default;
        windowType = type;
        windowDisplay = "grid";
    }


    /**
     * hide the popup window
     * 
     * @param event new value for the display
     */
    function hideWindow() {
        windowDisplay = "none";
        popupWindow = null;
    }

    /**
     * checks if the entered password is correct before allowing the user to edit his profile
     */
    async function passwordAuth() {
        infoDisplay = "none";
        messageText[0] = "";
        if (password === userDetails.password) {
            auth = true;
        } else {
            infoDisplay = "grid";
            backgroundColor = errorColor;
            messageText[0] = "Das eingegebene Passwort ist nicht korrekt!";
        }
    }
</script>

<svelte:head>
    <title>Account bearbeiten - DogNeeds</title>
</svelte:head>

<Header />
{#if windowDisplay !== "none"}
    <div class="window" style="display: {windowDisplay}">
        <svelte:component this={popupWindow} type={windowType} session={session} on:exit={hideWindow} /> <!--https://stackoverflow.com/questions/56431848/dynamically-loading-component-using-import-or-fetch-->
    </div>
{/if}
<div class="account">
    <div class="message" style="display: {infoDisplay}; background-color: {backgroundColor}">
        {#if backgroundColor == errorColor}
            <h1>Fehler</h1>
        {:else}
            <h1>Info</h1>
        {/if}
        {#each messageText as message}
            <p>{message}<br></p>
        {/each}
    </div>
    <a href="/account">Zurück zur Übersicht</a>
    {#await loadDetails()}
        <LoadingBar />
    {:then}
        {#if auth}
            <h1>Profil bearbeiten</h1>
            <div class="items">
                <div class="item" on:click={() => showWindow("name")} on:keypress={() => showWindow("name")}>
                    <EditAccountItem detail="name" value="{userDetails.firstName} {userDetails.lastName}" title="Name" img="name.png" /> <!--https://img.icons8.com/ios/256/name--v1.png-->
                </div>
                <div class="item" on:click={() => showWindow("email")} on:keypress={() => showWindow("email")}>
                    <EditAccountItem detail="email" value={userDetails.email} title="E-Mail-Adresse" img="mail.png" /> <!--https://img.icons8.com/ios/256/mail.png-->
                </div>
                <div class="item" on:click={() => showWindow("password")} on:keypress={() => showWindow("password")}>
                    <EditAccountItem detail="password" value={userDetails.password} title="Passwort" img="password.png" /> <!--https://img.icons8.com/ios/256/unlock-2.png-->
                </div>
                <div class="item" on:click={() => showWindow("address")} on:keypress={() => showWindow("address")}>
                    <EditAccountItem detail="address" value="{addressDetails.plz} {addressDetails.city} {addressDetails.street} {addressDetails.number}" title="Adresse" img="address.png" />
                </div>
                <div class="item" on:click={() => showWindow("delete")} on:keypress={() => showWindow("delete")}>
                    <EditAccountItem detail="delete" value="Lösche deinen Account" title="Löschung" img="delete.png" /> <!--https://img.icons8.com/ios/256/waste.png-->
                </div>
            </div>
        {:else}
            <div class="passwordAuth">
                <form action="" on:submit={passwordAuth}>
                    <h1>Password Authentifizierung</h1>
                        <div class="field">
                            <label for="password">Passwort:</label>
                            <input type="password" bind:value={password} placeholder="Passwort" autocomplete="new-password" required>
                        </div>
                    <div class="controls">
                        <button type="submit" class="submit">Bestätigen</button>    
                    </div>
                </form>
            </div>
        {/if}
    {/await}
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .passwordAuth{
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        background-color: white;
        border-radius: 15px;
    }
    .window {
        position: absolute;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 80%;
        backdrop-filter: blur(5px);
        background-color: rgba(255, 255, 255, 0.5);
        border-radius: 15px;
    }
    .account {
        display: grid;
        margin-left: 5%;
        margin-bottom: 14%;
        justify-content: center;
    }
    .message {
        border-radius: 10px;
        padding: 10px;
        margin-top: 20px;
        width: 95%;
        align-items: center;
        justify-content: center;
        text-align: center;
    }
    .message h1 {
        margin-top: 0;
        font-size: 2vw;
        color: white;
    }
    .message p {
        color: white;
        font-size: 1.2vw;
        margin: 2px;
    }
    .items {
        width: 100%;
        display: flex;
        flex-wrap: wrap;
        align-items: center;
    }
    a {
        font-size: 38px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
        text-align: center;
    }
    h1 {
        font-size: 42px;
    }
    input {
        font-size: 1vw;
        border: 1px solid black;
        border-radius: 5px;
        margin-bottom: 5%;
        top: 0;
        width: 100%;
        padding: 10px;
    }
    button {
        border: 1px solid black;
        border-radius: 10px;
        font-size: 1.5vw;
        background-color: white;
        padding: 10px;
        margin: 5px;
        margin-left: 40%;
        width: fit-content;
        color: #514538;

    }
    button:hover {
        background-color: blanchedalmond;
        cursor: pointer;
    }
    label {
        font-size: 1vw;
        margin-bottom: 7px;
        text-align: left;
        color: #514538;
    }
    .passwordAuth {
        display: grid;
        justify-content: center;
        align-items: center;
        margin-bottom: 60px; 
    }
    @media (max-width: 640px) {
        .window {
            height: 180%;
        }
        .message h1 {
            font-size: 28px;
        }
        .message p {
            font-size: 18px;
        }
        .account {
            margin-left: 0;
        }
        .items {
            display: grid;
            justify-content: center;
        }
        h1 {
            text-align: center;
        }
    }
</style>