<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { goto } from "$app/navigation";
    import { browser } from "$app/environment";
    import { onMount } from "svelte";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let name, description, price, category;
    let file;
    let categories = [];

    onMount(async () => {
        // check admin
        let request = await fetch("http://" + serverAddress + "/account/admingroup", {
            method: "POST",
            body: session
        });
        let result = await request.text();
        if (result !== "true") {
            goto("/account/login?l=2");
        }

        // load categories
        request = await fetch("http://" + serverAddress + "/category/get", {
            method: "POST"
        });
        result = JSON.parse(await request.text());
        
        // save categories
        for (let i = 0; i < result.length; i++) {
            categories[i] = {name: result[i].name};
        }
        category = categories[0].name;
    });

    /**
     * create the product
     */
    async function createProduct() {
        const formData = new FormData();
        formData.append("data", 
            JSON.stringify({
                session,
                category,
                name,
                description,
                price,
            }));
        
        if (file != null && file[0] != null) {
            formData.append("image", file[0]);
        }

        let request = await fetch("http://" + serverAddress + "/product/add", {
            method: "POST",
            body: formData
        });
        let result = await request.text();
        console.log(result);
        if (result === "success") {
            goto("/admin/products?r=3");
        } else {
            goto("/admin/products?r=4");
        }
    }
</script>

<svelte:head>
    <title>Produkt erstellen - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    <a href="/admin/products">Zurück zur Übersicht</a>
    <form action="">
        <div class="actions">
            <button type="submit" on:click={createProduct}>Produkt erstellen</button>
        </div>
        <div class="properties">
            <div class="category">
                <h2>Kategorie:</h2>
                <select bind:value={category} required>
                    {#each categories as c}
                        <option value={c.name}>{c.name}</option>
                    {/each}
                </select>
            </div>
            <div class="name">
                <h2>Name:</h2>
                <input type="text" bind:value={name} placeholder="Name" required>
            </div>
            <div class="description">
                <h2>Beschreibung:</h2>
                <input type="text" bind:value={description} placeholder="Beschreibung" required>
            </div>
            <div class="price">
                <h2>Preis:</h2>
                <input type="number" step="0.01" bind:value={price} placeholder="Preis" required>
            </div>
            <div class="img">
                <h2>Bild:</h2>
                <input type="file" accept="image/png, image/jpeg" bind:files={file} />
            </div>
        </div>
    </form>
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
    a {
        font-size: 42px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
    }
    button {
        margin: 10px;
        padding: 0 15px 0 15px;
        background-color: #fad8af;
        border-radius: 10px;
        font-size: 24px;
        font-weight: bold;
        height: 80px;
        color: #3b332b;
        width: 95%;
    }
    .actions button:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    .category, .name, .description, .price, .img {
        display: flex;
        align-items: center;
    }
    input, select, option {
        width: 80%;
        margin-left: 15px;
        font-size: 20px;
        height: fit-content;
    }
</style>