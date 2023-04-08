<script>
    import CategoryProduct from "$lib/product/CategoryProduct.svelte";
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import Error from "$lib/Error.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import { serverAddress } from "$lib/stores.js";

    // get url
    export let data;
    const { category } = data;

    let products = [];
    let error = "Die Kategorie konnte nicht gefunden werden!";

    /**
     * loads the products of the current category
     */
    async function loadProducts() {
        let request = await fetch("http://" + serverAddress + "/category/products", {
            method: "POST",
            body: category.category
        });
        let result = await request.text();
        if (result === "error;nocategory") {
            error = "Die Kategorie konnte nicht gefunden werden!";
        } else if (result === "error;empty") {
            error = "Die Kategorie enthält keine Produkte!";
        } else {
            // list of product-ids
            let productIds = result.split(";");
            for (let i = 0; i < productIds.length; i++) {
                // request for each product
                request = await fetch("http://" + serverAddress + "/product/request", {
                    method: "POST",
                    body: productIds[i] + ";name;price"
                });
                result = await request.text();
                let productDetails = result.split(";");

                // adding the product to the list
                products[i] = {productId: productIds[i], name: productDetails[0], url: "/product?p="+productIds[i], price: parseFloat(productDetails[1]).toFixed(2)}
            }
        }
    }
</script>

<svelte:head>
    <title>{category.category.toUpperCase()} - DogNeeds</title>
</svelte:head>

<Header />
{#await loadProducts()}
    <div class="catch">
        <LoadingBar />
    </div>
{:then}
    {#if products.length != 0}
        <div class="category">
            <h1>{category.category.toUpperCase()}</h1>
            <div class="product-items">
                {#each products as product}
                    <div style="margin: 20px;">
                        <svelte:component this={CategoryProduct} {...product} />
                    </div>           
                {/each}
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
<Footer />

<style>
    h1 {
		color: #514538;
		font-size: 48px;
		font-weight: 400;
        margin-bottom: 0;
        margin-left: 15px;
    }
    .category {
        margin-left: 5%;
        margin-bottom: 14%;
    }
    .catch {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 20px;
    }
    .product-items {
        display: flex;
        flex-wrap: wrap;
    }
    @media (max-width: 1400px) {
        .category {
            margin-bottom: 20%;
        }
        .product-items {
            display: grid;
        }
        h1 {
            font-size: 48px;
        }
    }
</style>