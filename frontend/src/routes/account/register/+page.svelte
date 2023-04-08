<script>
	import Header from "$lib/Header.svelte"
    import Footer from "$lib/Footer.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { goto } from "$app/navigation";
    import { browser } from "$app/environment";

    let firstName = "";
    let lastName = "";
    let plz = "";
    let city = "";
    let street = "";
    let number = "";
    let email = "";
    let emailValidation = "";
    let password = "";
    let passwordValidation = "";
    
    let errorMessage = [];
    let display = "none";

    async function handleSubmit() {
        let error = 0;
        cleanErrorMessages();
        
        if (containsInvalidSymbols(firstName) || containsInvalidSymbols(lastName)) {
            errorMessage[0] = "Dein Name darf keine Zahlen oder Sonderzeichen beinhalten!";
            error = 1;
        }
        // is not a number
        if (isNaN(plz)) {
            errorMessage[2] = "Die Postleitzahl muss eine Zahl sein! ";
            error = 1;
        }
        if (containsInvalidSymbols(city)) {
            errorMessage[3] = "Die Stadt darf keine Zahlen oder Sonderzeichen beinhalten!";
            error = 1;
        }
        if (containsInvalidSymbols(street)) {
            errorMessage[4] = "Die Straße darf keine Zahlen oder Sonderzeichen beinhalten!";
            error = 1;
        }
        if (email != emailValidation) {
            errorMessage[5] = "Die E-Mail-Adressen stimmen nicht überein! ";
            error = 1;
        }
        if (password !== passwordValidation) {
            errorMessage[6] = "Die Passwörter stimmen nicht überein! ";
            error = 1;
        }
        if (password.length < 6) {
            errorMessage[7] = "Dein Passwort sollte mindestens 6 Zeichen lang sein!";
        }

        // check if an error occured
        if (error == 1) {
            display = "grid";
            return;
        }

        display = "none";
        cleanErrorMessages();
   
        const response = await fetch("http://" + serverAddress + "/account/register", {
            method: "POST",
            body: JSON.stringify({
                email,
                password,
                firstName,
                lastName,
                plz,
                city,
                street,
                number
               
            })
        }).catch(function() {
            display = "grid";
            errorMessage[0] = "Es konnte keine Verbindung zum Server hergestellt werden!";
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
                const error = result.split(";")[1];
                if (error === "emailalreadyinuse") {
                    errorMessage[0] = "Diese E-Mail-Adresse ist bereits in Verwendung!";
                } else {
                    errorMessage[0] = "Es ist ein unerwarteter Fehler aufgetreten!";
                }
            }
        }
    }

    /**
     * check if text contains numbers or special characters
     * @param string the string to check
     * @returns true / false if containing invalid symbols
     */
    function containsInvalidSymbols(string) {
        // https://codingbeautydev.com/blog/javascript-check-if-string-contains-numbers/
        // Regex-Schreibweise, / starts & ends the expressen; \d digits 0-9
        // https://codingbeautydev.com/blog/javascript-check-if-string-contains-special-characters/

        const specialCharacters = /[`´!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/;
        return (/\d/.test(string) || specialCharacters.test(string));
    }

    /**
     * clean the error messages array
     */
    function cleanErrorMessages() {
        for (var i = 0; i < errorMessage.length; i++) {
            errorMessage[i] = "";
        }
    }
</script>

<svelte:head>
    <title>Registrieren - DogNeeds</title>
</svelte:head>

<Header />
<div class="register">
    <div class="error" style="display: {display};">
        <h1>Fehler</h1>
        {#each errorMessage as message}
            {#if message}
                <p>{message}<br></p>
            {/if}
        {/each}
    </div>
    <form action="" on:submit={handleSubmit}>
            <h1>Registrieren</h1>
            <div class="name-container">
                <div class="field">
                    <label for="firstName">Vorname:</label>
                    <input type="text" bind:value={firstName} placeholder="Vorname" autocomplete="given-name" required>
                </div>
                <div class="field">
                    <label for="lastName">Nachname:</label>
                    <input type="text" bind:value={lastName} placeholder="Nachname" autocomplete="family-name" required>
                </div>
            </div>
            <div class="address-container">
                <div class="city-container">
                    <div class="field">
                        <label for="plz">Postleitzahl:</label>
                        <input type="number" bind:value={plz} placeholder="Postleitzahl" inputmode="numeric" autocomplete="postal-code" required>
                    </div>
                    <div class="field">
                        <label for="city">Stadt:</label>
                        <input type="text" bind:value={city} placeholder="Stadt" autocomplete="address-level2" required>
                    </div>
                </div>
                <div class="home-container">
                    <div class="field">
                        <label for="street">Straße:</label>
                        <input type="text" bind:value={street} placeholder="Straße" autocomplete="street-address" required>
                    </div>
                    <div class="field">
                        <label for="number">Hausnummer:</label>
                        <input type="text" bind:value={number} placeholder="Hausnummer" required>
                    </div>
                </div>
            </div>
            <div class="email-container">
                <div class="field">
                    <label for="email">E-Mail-Adresse:</label>
                    <input type="email" bind:value={email} placeholder="E-Mail-Adresse" autocomplete="email" required>
                </div>
                <div class="field">
                    <label for="email2">E-Mail-Adresse wiederholen:</label>
                    <input type="email" bind:value={emailValidation} placeholder="E-Mail-Adresse wiederholen" autocomplete="email" required>
                </div>
            </div>
            <div class="password-container">
                <div class="field">
                    <label for="password">Passwort:</label>
                    <input type="password" bind:value={password} placeholder="Passwort" autocomplete="new-password" required>
                </div>
                <div class="field">
                    <label for="password2">Passwort wiederholen:</label>
                    <input type="password" bind:value={passwordValidation} placeholder="Passwort wiederholen" autocomplete="new-password" required>
                </div>
            </div>
            <div class="agree-container">
                <p>Mit der Registrierung stimme ich der <a href="/impressum">Datenschutzvereinbarung</a> und den <a href="/agb">AGBs</a> zu!</p>
            </div>
            
            <div class="controls">
                <button type="submit" class="submit">Registrieren</button>    
                <button type="button" on:click={() => goto("/account/login")}>Login</button>
            </div>
    </form>
</div>
<Footer />

<style>
    .register {
        display: flex;
        flex-direction: column;
        justify-content: left;
        align-items: center;
        margin-bottom: 60px;
    }
    .error {
        border: 2px solid #fa6969;
        background-color: #fa6969;
        border-radius: 10px;
        padding: 10px;
        margin-top: 20px;
    }
    .error h1 {
        margin-top: 0;
        font-size: 32px;
        color: white;
    }
    .error p {
        color: white;
        font-size: 22px;
        margin: 2px;
    }
    .field {
        display: flex;
        flex-direction: column;
        margin-right: 50px;
    }
    .name-container, .email-container, .password-container, .city-container, .home-container, .agree-container {
        display: flex;
        justify-content: center;
    }
    .agree-container {
        width: 100%;
    }
    .controls {
        display: flex;
        justify-content: left;
    }
    .submit {
        font-weight: bold;
    }
    input {
        font-size: 18px;
        border: 1px solid black;
        border-radius: 5px;
        margin-bottom: 5%;
        top: 0;
        width: 250px;
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
    p {
        color: #514538;
        font-size: 18px;
        word-wrap: break-word;
    }
    @media (max-width: 640px) {
        .name-container, .email-container, .password-container, .city-container, .home-container {
            display: flex;
            flex-direction: column;
        }
        .agree-container {
            width: 100%;
        }
        .register {
            margin-bottom: 80px;
        }
        .error h1 {
            font-size: 28px;
        }
        .error p {
            font-size: 18px;
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
            width: 100%;
        }
        button {
            font-size: 24px;
            width: 100%;
        }
        p {
            font-size: 18px;
        }
    }
</style>