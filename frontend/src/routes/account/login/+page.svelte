<script>
    import Header from "$lib/Header.svelte"
    import Footer from "$lib/Footer.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { goto } from "$app/navigation";
    import { browser } from "$app/environment";
    import { onMount } from "svelte";
    import { page } from "$app/stores";

    const successColor = "#9de683";
    const errorColor = "#fa6969";

    // session value in cookies; empty if non existing
    let session = browser ? window.sessionStorage.getItem("session") ?? "" : "";
    
    // fields for inputs
    let email = "";
    let password = "";

    // info / error message field
    let display = "none";
    let backgroundColor = successColor;
    let messageText = [
        ""
    ];

    /**
     * called on side load
    */
    onMount(() => {
        // if the user was logged out
        const logoutId = $page.url.searchParams.get("l");
        if (logoutId == 1) {
            // normal logout
            display = "grid";
            backgroundColor = successColor;
            messageText[0] = "Du hast dich erfolgreich ausgeloggt.";
        } else if (logoutId == 2) {
            // error logout
            display = "grid";
            backgroundColor = errorColor;
            messageText[0] = "Deine Session ist abgelaufen! Bitte logge dich erneut ein!";
        } else if (logoutId == 3) {
            // error logout
            display = "grid";
            backgroundColor = errorColor;
            messageText[0] = "Bitte logge dich erst ein, um auf den Einkaufswagen zugreifen zu können!";
        } else if (logoutId == 4) {
            // error logout
            display = "grid";
            backgroundColor = successColor;
            messageText[0] = "Dein Account wurde erfolgreich gelöscht.";
        }

        // check if the user is already logged in --> refer him to the account page
        if (!logoutId) {
            if (browser) {
                if (session !== "") {
                    goto("/account/");
                }
            }
        }
    });

    /**
     * Called when "login"-button is pressed
     */
    async function handleSubmit() {
        display = "none";
        messageText[0] = "";

        // email must inclode '@' to be valid
        if (!email.includes("@")) {
            display = "grid";
            messageText[0] = "Bitte gib eine gültige E-Mail-Adresse ein!";
            return;
        }

        // ask server if a user with this email & password exists
        const response = await fetch("http://" + serverAddress + "/account/login", {
            method: "POST",
            body: JSON.stringify({
                email,
                password
            })
        }).catch(function () {
            display = "grid";
            backgroundColor = errorColor;
            messageText[0] = "Es konnte keine Verbindung zum Server hergestellt werden!";
            return;
        });

        if (display === "none") {
            const result = await response.text();

            if (result.includes("success")) {
                // session for this user is stored in cookies
                const session = result.split(";")[1];
                if (browser) {
                    sessionStorage.setItem("session", session);
                    goto("/account/");
                }
            } else {
                // error
                display = "grid";
                backgroundColor = errorColor;
                const error = result.split(";")[1];
                if (error === "noaccountfound") {
                    messageText[0] = "Es konnte kein Account mit diesen Zugangsdaten gefunden werden!";
                } else {
                    messageText[0] = "Es ist ein unerwarteter Fehler aufgetreten!";
                }
            }
        }
    }
</script>

<svelte:head>
    <title>Einloggen - DogNeeds</title>
</svelte:head>

<Header />
<div class="login">
    <div class="message" style="display: {display}; background-color: {backgroundColor}">
        {#if backgroundColor == errorColor}
            <h1>Fehler</h1>
        {:else}
            <h1>Info</h1>
        {/if}
        {#each messageText as message}
            <p>{message}<br></p>
        {/each}
    </div>
    <form action="" on:submit={handleSubmit}>
            <h1>Einloggen</h1>
            <div class="email">
                <label for="email">E-Mail-Adresse eingeben:</label>
                <input type="text" bind:value={email} placeholder="E-Mail-Adresse" autocomplete="email" required>
            </div>
            <div class="password">
                <label for="password">Passwort eingeben:</label>
                <input type="password" bind:value={password} placeholder="Passwort" autocomplete="current-password" required>
            </div>
            <div class="controls">
                <button type="submit" class="submit">Login</button>
                <button type="button" on:click={() => goto("/account/register")}>Registrieren</button>    
            </div>
    </form>
</div>
<Footer />

<style>
    .login {
        display: flex;
        flex-direction: column;
        justify-content: left;
        align-items: center;
    }
    .message {
        border-radius: 10px;
        padding: 10px;
        margin-top: 20px;
    }
    .message h1 {
        margin-top: 0;
        font-size: 32px;
        color: white;
    }
    .message p {
        color: white;
        font-size: 22px;
        margin: 2px;
    }
    .email, .password {
        align-self: center;
        display: flex;
        flex-direction: column;
    }
    .controls {
        display: flex;
        justify-content: left;
    }
    .submit {
        font-weight: bold;
    }
    input {
        font-size: 20px;
        border: 1px solid black;
        border-radius: 5px;
        margin-bottom: 5%;
        top: 0;
        width: 300px;
        padding: 10px;
    }
    button {
        border: 1px solid black;
        border-radius: 10px;
        font-size: 25px;
        background-color: white;
        padding: 10px;
        margin: 5px;
        width: fit-content;
        color: #514538;
    }
    button:hover {
        background-color: blanchedalmond;
        cursor: pointer;
    }
    h1 {
		color: #514538;
		font-size: 48px;
		font-weight: 100;
        margin-bottom: 10px;
	}
    label {
        font-size: 20px;
        margin-bottom: 7px;
        text-align: left;
        color: #514538;
    }
    @media (max-width: 640px) {
        .login {
            display: flex;
            flex-direction: column;
        }
        form {
            width: 100%;
        }
        h1 {
            font-size: 48px;
        }
        label {
            font-size: 24px;
        }
        input {
            font-size: 20px;
            width: 95%;
        }
        button {
            width: 100%;
            font-size: 28px;
        }
        .message h1 {
            font-size: 28px;
        }
        .message p {
            font-size: 18px;
        }
    }
</style>