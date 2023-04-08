<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let actions = [
        {name: "Kategorien verwalten", url: "/admin/categories"},
        {name: "Produkte verwalten", url: "/admin/products"},
        {name: "Nutzer verwalten", url: "/admin/users"},
        {name: "Support-Anfragen", url: "/admin/support"}
    ];

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
</script>

<svelte:head>
    <title>Admin-Bereich - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    <h1>Admin-Bereich</h1>
    <div class="actions">
        {#each actions as action}
            <a href={action.url}>{action.name}</a>
        {/each}
    </div>
</div>
<div class="session">
    <p>Session: {session}</p>
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .admin {
        display: grid;
        margin-bottom: 4%;
        justify-content: center;
        text-align: center;
    }
    .actions {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-evenly;
    }
    .session {
        text-align: center;
    }
    h1 {
        font-size: 42px;
    }
    a {
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 24px;
        margin: 10px;
        padding: 0 15px 0 15px;
        background-color: #fad8af;
        border-radius: 10px;
        height: 80px;
        color: #3b332b;
        text-decoration: none;
        box-shadow: 2px 2px 0 black;
    }
    a:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    @media (max-width: 640px) {
        .actions {
            flex-direction: column;
        }
    }
</style>