<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import AdminCategoryItem from "$lib/category/AdminCategoryItem.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let categories = [];
    let showAdd = false;
    let categoryName;

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

        // load categories
        response = await fetch("http://" + serverAddress + "/category/get", {
            method: "POST"
        });
        result = JSON.parse(await response.text());
        
        // save categories
        for (let i = 0; i < result.length; i++) {
            categories[i] = {name: result[i].name};
        }
    });

    /**
     * toggles the visibilty of the add-category field
     */
    function toggleAdd() {
        showAdd = !showAdd;
    }

    /**
     * add a new category
    */
    async function addCategory() {
        if (!categoryName) {
            alert("Bitte gib eine Kategoriebezeichnung ein!");
            return;
        }

        let response = await fetch("http://" + serverAddress + "/category/add", {
            method: "POST",
            body: JSON.stringify({
                session,
                categoryName
            })
        });
        let result = await response.text();
        if (result === "success") {
            location.reload();
        } else if (result === "error;exists") {
            alert("Diese Kategorie gibt es bereits!");
        } else {
            alert("Es ist ein unerwarteter Fehler aufgetreten!");
        }
    }
</script>

<svelte:head>
    <title>Kategorien verwalten - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    <a href="/admin/">Admin-Bereich</a>
    <div class="categories">
        <div class="title">
            <h2>Kategorien</h2>
            <button on:click={toggleAdd} style="display: {showAdd ? "none" : "initial"}">
                <img src="/images/admin/add.png" alt="Kategorie hinzufügen"> <!--https://img.icons8.com/ios-filled/512/plus-2-math.png-->
            </button>
        </div>
        <div class="add" style="display: {showAdd ? "flex" : "none"}">
            <input type="text" placeholder="Kategoriebezeichnung" bind:value={categoryName}>
            <button on:click={addCategory}>
                <img src="/images/admin/add.png" alt="Kategorie hinzufügen"> <!--https://img.icons8.com/ios-filled/512/plus-2-math.png-->
            </button>
        </div>
        {#each categories as category}
            <AdminCategoryItem categoryName={category.name} />
        {/each}
    </div>
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
    .categories {
        display: grid;
    }
    .title {
        display: flex;
        justify-content: center;
    }
    .add {
        justify-content: center;
        align-items: center;
        margin: 20px;
    }
    a {
        font-size: 42px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
    }
    input {
        font-size: 24px;
        margin-right: 5px;
    }
    button {
        background-color: white;
        border: none;
    }
    button:hover {
        cursor: pointer;
    }
    img {
        width: 40px;
        height: auto;
    }
</style>