<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import Error from "$lib/Error.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies
    let users = [];
    let filteredUsers = [];

    let search;

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
     * loads all users
     */
    async function loadUsers() {
        // load users
        let response = await fetch("http://" + serverAddress + "/account/getAll", {
            method: "POST"
        });
        let result = JSON.parse(await response.text());
        
        // save users
        for (let i = 0; i < result.length; i++) {
            users[i] = {id: result[i].uid, firstName: result[i].firstName, lastName: result[i].lastName, email: result[i].email};
        }
        filteredUsers = users;
    }

    function filterUsers() {
        if (search) {
            filteredUsers = [];
            var counter = 0;
            for (let i = 0; i < users.length; i++) {
                if (users[i].firstName.toLowerCase().includes(search.toLowerCase()) || 
                users[i].lastName.toLowerCase().includes(search.toLowerCase()) || 
                users[i].email.toLowerCase().includes(search.toLowerCase()) || 
                (users[i].firstName.toLowerCase() + " " + users[i].lastName.toLowerCase()).includes(search.toLowerCase())) {
                    filteredUsers[counter] = users[i];
                    counter++;
                }
            }
        } else {
            filteredUsers = users;
        }
    }
</script>

<svelte:head>
    <title>Nutzer verwalten - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    <a href="/admin">Admin-Bereich</a>
    {#await loadUsers()}
        <LoadingBar />
    {:then}
        <div class="search">
            <h2>Suche:</h2>
            <input type="text" bind:value={search} on:input={filterUsers} placeholder="Name/E-Mail">
        </div>
        <div class="users">
            {#each filteredUsers as user}
                <div class="user">
                    <p>{user.firstName} {user.lastName}</p>
                    <p>{user.email}</p>
                    <button on:click={() => goto("/admin/users/edit?u=" + user.id)}>Zum Nutzer</button>
                </div>
            {/each}
        </div>
    {:catch}
        <Error error="Die Nutzer konnten nicht geladen werden!" />
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
        margin-bottom: 4%;
        justify-content: center;
        text-align: center;
    }
    .search {
        display: flex;
        align-items: center;
    }
    .users {
        display: grid;
        align-items: center;
        justify-content: center;
    }
    .user {
        width: 100%;
        height: fit-content;
        border-radius: 10px;
        padding: 10px;
        margin-top: 10px;
        display: grid;
        text-align: center;
        border: 1px solid black;
    }
    .user:hover {
        box-shadow: 0px 0px 5px 0px #000000;
    }
    a {
        font-size: 42px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
    }
    input {
        width: 100%;
        margin-left: 15px;
        font-size: 20px;
        height: fit-content;
    }
    button {
        margin: 5px;
        padding: 5px;
        border-radius: 10px;
        border: 1px solid black;
        font-size: 15px;
        height: 50px;
        color: #3b332b;
        background-color: white;
    }
    button:hover {
        cursor: pointer;
        background-color: #fad8af;
    }
</style>