<script>
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    // session value in cookies; empty if non existing
    const session = browser ? window.sessionStorage.getItem("session") ?? "" : "";

    let categories = [];
    let admin = false;

    async function handleClick(i) {
        goto("/category/" + categories[i].url);
        setTimeout(function () {
            location.reload();
        }, 50);
    }

    onMount(async () => {
        // load categories
        let response = await fetch("http://" + serverAddress + "/category/get", {
            method: "POST"
        });
        let result = JSON.parse(await response.text());
        
        // save categories
        for (let i = 0; i < result.length; i++) {
            categories[i] = {name: result[i].name, url: result[i].name.toLowerCase()};
        }

        if (session) {
            // check admin
            response = await fetch("http://" + serverAddress + "/account/admingroup", {
                method: "POST",
                body: session
            });
            result = await response.text();
            if (result === "true") {
                admin = true;
            }
        }
        
    });
</script>

<!--https://www.youtube.com/watch?v=bk3Y4heVdFs&ab_channel=Mr.WebDesigner-->
<header>
    <a class="title" href="/">DogNeeds</a>

    <input type="checkbox" id="menu-bar">
    <label for="menu-bar">Menü</label>

    <nav class="navbar">
        <ul>
            <li><a href="/">Home</a></li>
            <li><a href="">Kategorien+</a>
                <ul>
                    {#each categories as category, i}
                        <li><a href={"/category/" + category.url} on:click={() => handleClick(i)}>{category.name}</a></li>
                    {/each}
                </ul>
            </li>
            <li><a href="/cart">Einkaufswagen</a></li>
            {#if admin}
                <li><a href="/account">Account+</a>
                    <ul>
                        <li><a href="/admin/">Admin-Bereich</a></li>
                    </ul>
                </li>
            {:else}
                <li><a href="/account">Account</a></li>
            {/if}
        </ul>
    </nav>
</header>

<style>
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
        text-decoration: none;
	}
    header {
        position: sticky;
        top: 0;
        left: 0;
        right: 0;
        background-color: #FFC075;
        box-shadow: 0 5px 10px rgba(0, 0, 0, .1);
        border-radius: 10px;
        padding: 10px 5%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        z-index: 1000;
    }
    a {
        color: #514538;
    }
    header nav ul li a:hover {
        background-color: #fad8af; 
        border-radius: 20px;
    }
    .title {
        font-weight: bolder;
        font-size: 32px;
    }
    header nav ul {
        list-style: none;
    }
    header nav ul li {
        position: relative;
        float: left;
    }
    header nav ul li a {
        font-size: 22px;
        padding: 20px;
        display: block;
    }
    header nav ul li ul {
        position: absolute;
        display: none;
        left: 0;
        width: fit-content;
        background-color: #FFC075;
        border: none;
        border-radius: 20px;
    }
    header nav ul li ul li {
        width: 100%;
    }
    header nav ul li:focus-within > ul,
    header nav ul li:hover > ul {
        display: initial;
    }
    #menu-bar {
        display: none;
    }
    header label {
        font-size: 20px;
        color: #514538;
        cursor: pointer;
        display: none;
    }

    @media (max-width: 830px) {
        header {
            padding: 20px;
        }
        header label {
            display: initial;
        }
        header nav {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: #fff;
            border-top: 1px solid rgba(0, 0, 0, .1);
            display: none;
        }
        header nav ul li {
            width: 100%;
        }
        header nav ul li ul {
            position: relative;
            width: 100%;
        }
        header nav ul li ul li {
            background-color: #eee;
            border-radius: 20px;
        }
        #menu-bar:checked ~ nav {
            display: initial;
        }
    }
</style>