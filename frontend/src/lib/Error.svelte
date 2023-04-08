<script>
    export let error = "Fehler";
    export let img = "/images/messages/error.png"; //https://img.icons8.com/ios/512/cancel.png
</script>

<div class="error">
    <img src={img} alt="Error symbol">
    <p>{error}</p>
</div>

<style>
    .error {
        display: flex;
        margin: 0;
        align-items: center;
    }
    img {
        width: 32px;
        height: 32px;
    }
    p {
        font-size: 24px;
        margin-left: 10px;
        color: #514538;
    }
</style>