<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import Error from "$lib/Error.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import { serverAddress, defaultProductPicture } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount, onDestroy } from "svelte";
    import { page } from "$app/stores";

    let session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let productDetails;
    let productId;
    let name;
    let price;
    let description;
    let img;
    let category;

    let categories = [];

    let file;

    let error = "Es wurde kein Produkt spezifiziert.";

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

        productId = $page.url.searchParams.get("p");
    });

    /**
     * load the details of the product
     */
     async function loadProduct() {
        let request = await fetch("http://" + serverAddress + "/product/request", {
            method: "POST",
            body: productId + ";name;price;description;category"
        }).catch(() => {
            error = "Es konnte keiner Verbindung zum Server hergestellt werden!";
        });
        let result = await request.text();
        if (result == "error;notfound") {
            error = "Das Produkt konnte nicht gefunden werden!";
        } else {
            error = "";
            productDetails = result.split(";");
            name = productDetails[0];
            price = productDetails[1];
            description = productDetails[2];
            if (productDetails[3] != null) {
                category = productDetails[3];
            }

            request = await fetch("http://" + serverAddress + "/product/img", {
                method: "POST",
                body: productId
            });
            result = await request.blob();
            if (result.size > 0) {
                img = URL.createObjectURL(result);
            } else {
                img = defaultProductPicture;
            }
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
    }

    /**
     * edit product
     */
    async function editProduct() {
        let changes = {
            session,
            productId
        };
        if (productDetails[0] !== name) {
            changes.name = name;
        }
        if (productDetails[1] != price) {
            changes.price = price;
        }
        if (productDetails[2] !== description) {
            changes.description = description;
        }
        if (productDetails[3] !== category) {
            changes.category = category;
        }

        const formData = new FormData();
        formData.append("changes", JSON.stringify(changes));
        if (file != null && file[0] != null) {
            formData.append("image", file[0]);
        }

        await fetch("http://" + serverAddress + "/product/edit", {
            method: "POST",
            body: formData
        });
        location.reload();
    }

    /**
     * update the picture preview
     */
    async function changePicture() {
        let reader = new FileReader();
        reader.readAsDataURL(file[0]);
        reader.onload = function() {
            img = reader.result;
        };
    }

    /**
     * deletes a product
     */
    async function deleteProduct() {
        const confirmation = window.confirm("Soll das Produkt wirklich gelöscht werden? Diese Aktion kann nicht rückgängig gemacht werden!");
        if (confirmation) {
            let request = await fetch("http://" + serverAddress + "/product/delete", {
                method: "POST",
                body: JSON.stringify({
                    session,
                    productId
                })
            });
            const result = await request.text();
            if (result === "success") {
                goto("/admin/products?r=1");
            } else {
                goto("admin/products?r=2");
            }
        }
    }

    function cleanup() {
        if (img) {
            URL.revokeObjectURL(img);
        }
    }
    
    onDestroy(cleanup);
</script>

<svelte:head>
    <title>{name} - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    <a href="/admin/products">Zurück zur Übersicht</a>
    {#if productId}
        {#await loadProduct()}
            <div class="catch">
                <LoadingBar />
            </div>
        {:then} 
            {#if !error}
                <div class="actions">
                    <button><label class="button" for="file-input">Bild ändern</label></button>
                    <input type="file" accept="image/png, image/jpeg" id="file-input" bind:files={file} on:change={changePicture} />
                    <button on:click={editProduct}>Änderungen speichern</button>
                    <button on:click={deleteProduct}>Produkt löschen</button>
                </div>
                <div class="product">
                    <div class="image">
                        <img src="{img}" alt="">
                    </div>
                    <div class="details">
                        <div class="category">
                            <h2>Kategorie:<h2>
                            <select bind:value={category} required>
                                {#each categories as c}
                                    <option value={c.name}>{c.name}</option>
                                {/each}
                            </select>
                        </div>
                        <div class="title">
                            <h2>Name:<h2>
                            <input type="text" bind:value={name} placeholder={name}>
                        </div>
                        <div class="description">
                            <h2>Beschreibung:</h2>
                            <input type="text" bind:value={description} placeholder={description}>
                        </div>
                        <div class="price">
                            <h2>Preis:</h2>
                            <input type="number" bind:value={price} placeholder={price}>
                        </div>
                    </div>
                </div>
            {:else}
                <div class="catch">
                    <Error error={error} />
                </div>
            {/if}
        {:catch}
            <div class="catch">
                <Error error={error} />
            </div>
        {/await}
    {:else}
    <div class="catch">
        <Error error={error} />
    </div>
    {/if}
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
    .category, .title, .description, .price {
        margin: 25px;
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
        font-size: 1.5vw;
        height: 80px;
        color: #3b332b;
    }
    .actions button:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    label:hover {
        cursor: pointer;
    }
    img {
        width: 480px;
        height: auto;
        margin-top: 10px;
        border: 1px solid black;
    }
    h2 {
        font-size: 32px;
        margin: 0;
    }
    input, select, option {
        font-size: 24px;
        width: 80%;
        margin-top: 10px;
    }
    #file-input {
        display: none;
    }
</style>