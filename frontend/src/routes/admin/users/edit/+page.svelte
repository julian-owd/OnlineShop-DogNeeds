<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import OrderItem from "$lib/account/order/OrderItem.svelte";
    import Error from "$lib/Error.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";
    import { page } from "$app/stores";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let userId;
    let userResult;
    let userDetails = {};
    let addressDetails = {};
    let userGroups = [];
    let userGroup;
    let orderIds = [];
    let orderError;

    onMount(async () => {
        // check admin
        let response = await fetch("http://" + serverAddress + "/account/admingroup", {
            method: "POST",
            body: session
        });
        let result = await response.text();
        if (result !== "true") {
            goto("/account/login?l=2");
        }
    });

    /**
     * loads the details of the current user
     */
    async function loadUser() {
        userId = $page.url.searchParams.get("u");
        let request = await fetch("http://" + serverAddress + "/account/get", {
            method: "POST",
            body: JSON.stringify({
                session,
                userId
            })
        });
        userResult = JSON.parse(await request.text());
        userDetails.email = userResult.email;
        userDetails.password = userResult.password;
        userDetails.firstName = userResult.firstName;
        userDetails.lastName = userResult.lastName;
        userGroup = userResult.ugid.name;
        addressDetails.plz = userResult.aid.plz;
        addressDetails.city = userResult.aid.city;
        addressDetails.street = userResult.aid.street;
        addressDetails.number = userResult.aid.number;

        request = await fetch("http://" + serverAddress + "/account/getUserGroups");
        let result = JSON.parse(await request.text());
        userGroups = result;

        request = await fetch("http://" + serverAddress + "/order/getOrders", {
            method: "POST",
            body: JSON.stringify({
                session,
                userId
            })
        });
        result = JSON.parse(await request.text());
        if (result.includes("error")) {
            orderError = "Deine Bestellungen konnten nicht geladen werden!";
            return;
        }
        if (result.length == 0) {
            orderError = "Es sind noch keine Bestellungen für deinen Account vorhanden!";
            return;
        }
        orderIds = result;
    }

    /**
     * edits the selected user
     */
    async function editUser() {
        let changes = {
            session,
            userId
        };
        if (userResult.email !== userDetails.email) {
            changes.email = userDetails.email;
        }
        if (userResult.password !== userDetails.password) {
            changes.password = userDetails.password;
        }
        if (userResult.firstName !== userDetails.firstName) {
            changes.firstName = userDetails.firstName;
        }
        if (userResult.lastName !== userDetails.lastName) {
            changes.lastName = userDetails.lastName;
        }
        if (userResult.aid.plz !== addressDetails.plz) {
            changes.addressPlz = addressDetails.plz;
        }
        if (userResult.aid.city !== addressDetails.city) {
            changes.addressCity = addressDetails.city;
        }
        if (userResult.aid.street !== addressDetails.street) {
            changes.addressStreet = addressDetails.street;
        }
        if (userResult.aid.number !== addressDetails.number) {
            changes.addressNumber = addressDetails.number;
        }
        if (userResult.ugid.name !== userGroup) {
            changes.userGroup = userGroup;
        }
        let request = await fetch("http://" + serverAddress + "/account/edit", {
            method: "POST",
            body: JSON.stringify(changes)
        });
        let result = await request.text();
        if (result === "success") {
            alert("Die Änderungen wurden übernommen.");
            goto("/admin/users");
        } else {
            if (result === "error;emailnotavailable") {
                alert("Diese E-Mail-Adresse ist nicht verfügbar!");
            } else {
                alert("Deine Änderungen konnten nicht übernommen werden!")
            }
        }
    }

    /**
     * deletes a user
     */
    async function deleteUser() {
        const confirmation = window.confirm("Soll der Nutzer wirklich gelöscht werden? Diese Aktion kann nicht rückgängig gemacht werden!");
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
                alert("Der Nutzer wurde gelöscht!")
                goto("/admin/users");
            } else {
                alert("Der Nutzer konnte nicht gelöscht werden!")
            }
        }
    }
</script>

<svelte:head>
    <title>{userDetails.firstName} {userDetails.lastName} - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    <a href="/admin/users">Zurück zur Übersicht</a>
    {#await loadUser()}
        <LoadingBar />
    {:then} 
        {#if userResult.firstName}
            <div class="actions">
                <button on:click={editUser}>Änderungen speichern</button>
                <button on:click={deleteUser}>Nutzer entfernen</button>
            </div>
            <table>
                <tr>
                    <td>Vorname:</td>
                    <td><input type="text" bind:value={userDetails.firstName} required></td>
                    <td>Nachname:</td>
                    <td><input type="text" bind:value={userDetails.lastName} required></td>
                </tr>
                <tr>
                    <td>Email:</td>
                    <td><input type="email" bind:value={userDetails.email} required></td>
                    <td>Password:</td>
                    <td><input type="password" bind:value={userDetails.password} required></td>
                </tr>
            </table>
            <table>
                <tr>
                    <td>Gruppe:</td>
                    <td><select bind:value={userGroup}>
                        {#each userGroups as group}
                            <option value={group}>{group}</option>
                        {/each}
                    </select></td>
                </tr>
            </table>
            <table>
                <tr>
                    <td>PLZ:</td>
                    <td><input type="text" bind:value={addressDetails.plz} required></td>
                    <td>Stadt:</td>
                    <td><input type="text" bind:value={addressDetails.city} required></td>
                </tr>
                <tr>
                    <td>Straße:</td>
                    <td><input type="text" bind:value={addressDetails.street} required></td>
                    <td>Nummer:</td>
                    <td><input type="text" bind:value={addressDetails.number} required></td>
                </tr>
            </table>
            <div class="orders">
                {#if orderIds.length > 0}
                    <h1>Bestellungen</h1>
                    <div class="order-items">
                        {#each orderIds as orderId}
                            <OrderItem orderId={orderId} />
                        {/each}
                    </div>
                {:else}
                    <Error error={orderError} />
                {/if}
            </div>
        {:else}
            <Error error="Der Nutzer konnte nicht geladen werden!" />
        {/if}
    {:catch}
        <Error error="Der Nutzer konnte nicht geladen werden!" />
    {/await}
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .admin {
        display: grid;
        margin-left: 5%;
        margin-bottom: 5%;
        justify-content: center;
        text-align: center;
    }
    .actions {
        display: flex;
    }
    a {
        font-size: 42px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
    }
    .actions button {
        margin: 10px;
        padding: 0 15px 0 15px;
        background-color: #fad8af;
        border-radius: 10px;
        font-size: 24px;
        height: 80px;
        color: #3b332b;
        width: 95%;
    }
    .actions button:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    table td {
        padding: 10px;
        word-wrap: break-word;
        text-align: left;
        min-width: 80px;
    }
    input {
        width: 95%;
        font-size: 18px;
    }
    select, option {
        width: 99%;
        font-size: 18px;
    }
    .orders {
        display: flex;
        flex-direction: column;
        justify-content: center;
        width: 70rem;
    }
</style>