<script>
    import { onMount } from "svelte";
    
    export let detail;
    export let value;
    export let title;
    export let img;

    onMount(() => {
        if (detail === "password") {
            var replace = "";
            for (let i = 0; i < value.length; i++) {
                replace += "⦿";
            }
            value = replace;
        }
    });
</script>

<div class="item" >
    <div class="header">
        <div class="title">
            <h2>{title}</h2>
        </div>
        <div class="image">
            <img src="/images/account/{img}" alt="Icon for {title}">
        </div>
    </div>
    <div class="value">
        <p>{value}</p>
    </div>
</div>

<style>
    * {
        color: #514538;
    }
    .item {
        border: 1px solid black;
        border-radius: 10px;
        box-shadow: 0px 0px 10px 0px gray;
        width: 320px;
        height: auto;
        padding: 10px;
        margin: 15px;
    }
    .item:hover {
        box-shadow: 0px 0px 10px 0px black;
        cursor: pointer;
    }
    .header {
        display: flex;
    }
    .title {
        margin-left: 1%;
        text-align: left;
        width: 100%;
    }
    .image {
        margin-right: 1%;
        float: right;
    }
    .value {
        word-break: break-all;
    }
    h2 {
        margin-top: 5px;
    }
    img {
        margin-top: 0;
        width: 50px;
        height: auto;
    }
</style>