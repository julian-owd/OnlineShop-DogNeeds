<script>
   	import { createEventDispatcher } from 'svelte';
    import LoadingBar from "$lib/LoadingBar.svelte";
    import Error from "$lib/Error.svelte";
    import { serverAddress, checkAuth } from "$lib/stores.js";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    export let type;
    export let session;

    const dispatch = createEventDispatcher();

    let title = "Lädt";
    let userId;
    let userResult;
    let keys = [];
    let values = [];
    let submitting = false;
    let error;

    onMount(async () => {
        if (!checkAuth()) {
            goto("/account/login?l=2");
        }
    });

    /**
     * adapt the window to the required type (name, email, ...)
     */
    async function prepareWindow() {
        let request = await fetch("http://" + serverAddress + "/account/request", {
            method: "POST",
            body: session + ";id"
        });
        userId = await request.text();
        request = await fetch("http://" + serverAddress + "/account/get", {
            method: "POST",
            body: JSON.stringify({
                session,
                userId
            })
        });
        userResult = JSON.parse(await request.text());

        if (type === "name") {
            title = "Namen ändern";
            keys[0] = "Vorname(n)";
            keys[1] = "Nachname";
            values[0] = userResult.firstName;
            values[1] = userResult.lastName;
        } else if (type === "email") {
            title = "E-Mail-Adresse ändern";
            keys[0] = "E-Mail-Adresse";
            keys[1] = "E-Mail-Adresse wiederholen";
            values[0] = userResult.email;
            values[1] = userResult.email;
        } else if (type === "password") {
            title = "Passwort ändern";
            keys[0] = "Aktuelles Passwort eingeben";
            keys[1] = "Neues Passwort eingeben";
            keys[2] = "Neues Passwort wiederholen";
            values[0] = "";
            values[1] = "";
            values[2] = "";
        } else if (type === "address") {
            title = "Adresse ändern";
            keys[0] = "PLZ";
            keys[1] = "Stadt";
            keys[2] = "Straße";
            keys[3] = "Hausnummer";
            values[0] = userResult.aid.plz;
            values[1] = userResult.aid.city;
            values[2] = userResult.aid.street;
            values[3] = userResult.aid.number;
        } else if (type === "delete") {
            title = "Account löschen";
            keys[0] = "Aktuelles Passwort eingeben";
            values[0] = "";
        } else {
            title = "Fehler!";
        }
    }

    /**
     * detect the changes and check if they're correct
     */
    async function prepareChanges() {
        submitting = true;
        var changes = {
            session,
            userId
        };
        var changed = false;

        if (type === "name") {
            if (containsInvalidSymbols(values[0]) || containsInvalidSymbols(values[1])) {
                error = "Dein Name darf keine Zahlen oder Sonderzeichen beinhalten!";
            }
            if (values[0] !== userResult.firstName) {
                changes.firstName = values[0];
                changed = true;
            }
            if (values[1] !== userResult.lastName) {
                changes.lastName = values[1];
                changed = true;
            }
        } else if (type === "email") {
            if (values[0] !== values[1]) {
                error = "Die beiden E-Mail-Adressen müssen übereinstimmen!";
            }
            if (!values[0].includes("@")) {
                error = "Die neue E-Mail-Adresse muss ein '@' beinhalten!";
            }
            if (values[0] !== userResult.email) {
                changes.email = values[0];
                changed = true;
            }
        } else if (type === "password") {
            if (values[1] !== values[2]) {
                error = "Die beiden neuen Passwörter müssen übereinstimmen!";
            }
            if (values[1].length < 6) {
                error = "Das neue Passwort muss länger als 6 Zeichen sein!";
            }
            if (values[0] !== userResult.password) {
                error = "Das aktuelle Passwort ist nicht korrekt!";
            }
            if (values[1] !== userResult.password) {
                changes.password = values[1];
                changed = true;
            }
        } else if (type === "address") {
            if (isNaN(values[0])) {
                error = "Die Postleitzahl muss eine Zahl sein!";
            }
            if (containsInvalidSymbols(values[1])) {
                error = "Die Stadt darf keine Zahlen oder Sonderzeichen beinhalten!";
            }
            if (containsInvalidSymbols(values[2])) {
                error = "Die Straße darf keine Zahlen oder Sonderzeichen beinhalten!";
            }
            if (values[0] !== userResult.aid.plz) {
                changes.addressPlz = values[0];
                changed = true;
            }
            if (values[1] !== userResult.aid.city) {
                changes.addressCity = values[1];
                changed = true;
            }
            if (values[2] !== userResult.aid.street) {
                changes.addressStreet = values[2];
                changed = true;
            }
            if (values[3] !== userResult.aid.number) {
                changes.addressNumber = values[3];
                changed = true;
            }
        } else if (type === "delete") {
            if (values[0] !== userResult.password) {
                error = "Das aktuelle Passwort ist nicht korrekt!";
            } else {
                const confirmation = window.confirm("Möchtest du deinen Account wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.");
                if (confirmation) {
                    let request = await fetch("http://" + serverAddress + "/account/delete", {
                        method: "POST",
                        body: JSON.stringify({
                            session,
                            userId
                        })
                    });
                    const result = await request.text();
                    if (result === "success") {
                        goto("/account/login?l=4");
                    } else {
                        await goto("/account/edit?r=2");
                        location.reload();
                    }
                    return;
                }
            }
        }

        // check if an error has occured
        if (error) {
            submitting = false;
            return;
        }

        // submitting changes to server
        if (changed) {
            change(changes);
        } else {
            submitting = false;
        }
    }

    /**
     * submit the changes to server
     * @param changes changes
     */
    async function change(changes) {
        var request = await fetch("http://" + serverAddress + "/account/edit", {
            method: "POST",
            body: JSON.stringify(changes)
        }).catch(function () {
            error = "Deine Änderungen konnten nicht übernommen werden!";
            return;
        });
        var result = await request.text();
        if (result === "success") {
            await goto("/account/edit?r=1");
            location.reload();
        } else {
            if (result === "error;emailnotavailable") {
                error = "Diese E-Mail-Adresse ist nicht verfügbar!";
            } else {
                error = "Deine Änderungen konnten nicht übernommen werden!";
            }
        }
        submitting = false;
    }

    /**
     * close the window
     */
    function exitWindow() {
        dispatch("exit", {
            text: "none"
        });
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
</script>

<div class="window">
    <div class="header">
        <h1>{title}</h1>
        <button on:click={exitWindow}>
            <img src="/images/account/close.png" alt="Close window"> <!--https://img.icons8.com/color/256/close-window.png-->
        </button>
    </div>
    {#await prepareWindow()}
        <LoadingBar />
    {:then}
        <!--if currently sending the changes to the server-->
        {#if !submitting}
            {#if !error}
                <div class="body">
                    <div class="items">
                        {#each keys as key, i}
                            <div class="item">
                                <h2>{key}:</h2>
                                {#if type === "password"}
                                    <input type="password" bind:value={values[i]}>
                                {:else if type === "email"}
                                    <input type="email" bind:value={values[i]}>
                                {:else if type === "delete"}
                                    <input type="password" bind:value={values[0]}>
                                {:else}
                                    <input type="text" bind:value={values[i]}>
                                {/if}
                            </div>
                        {/each}
                        {#if type === "delete"}
                            <h2>Hinweis</h2>
                            <p>Diese Aktion kann nicht rückgängig gemacht werden!</p>
                        {/if}
                    </div>
                    {#if type === "delete"}
                        <button on:click={prepareChanges}>Account löschen</button>
                    {:else}
                        <button on:click={prepareChanges}>Änderungen speichern</button>
                    {/if}
                </div>
            {:else}
                <Error error={error} />
            {/if}
        {:else}
            <LoadingBar />
        {/if}
    {:catch}
        <Error error="Es ist ein Fehler aufgetreten!" />
    {/await}
</div>

<style>
    * {
        color: #514538;
    }
    .window {
        border-radius: 10px;
        background-color: white;
        box-shadow: 0px 0px 10px 0px gray;
        padding: 20px;
    }
    .item {
        align-items: center;
        display: flex;
    }
    .header {
        display: flex;
        justify-content: space-between;
        align-items: start;
    }
    .header h1 {
        margin-top: 5px;
        text-decoration: underline;
    }
    .header button {
        border: none;
        background-color: white;
    }
    button:hover {
        cursor: pointer;
    }
    img {
        width: 48px;
        height: auto;
    }
    input {
        font-size: 24px;
        width: 80%;
        margin-left: 20px;
        border: none;
        padding: 5px;
        border-bottom: 2px solid lightgray;
    }
    input:focus {
        outline: none;
    }
    p {
        margin-left: 10px;
        font-size: 24px;
    }
    .body button {
        margin: 5px;
        margin-top: 15px;
        padding: 10px;
        background-color: #fad8af;
        border-radius: 10px;
        font-size: 24px;
        width: 100%;
        height: fit-content;
        color: #3b332b;
    }
    .body button:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    @media (max-width: 640px) {
        .window {
            max-width: 85%;
        }
        .item {
            display: grid;
        }
        .body button {
            height: fit-content;
        }
    }
</style>