<script>
    export let icon;
    export let alt;
    export let text;
</script>

<div class="promise">
    <div class="icon">
        <img src={icon} alt={alt}>
    </div>
    <div class="text">
        <p>{text}</p>
    </div>
</div>

<style>
    .promise {
        width: 280px;
        height: fit-content;
        border-radius: 10px;
        padding: 5px;
        box-shadow: 0px 0px 5px 0px #000000;
        text-align: center;
    }
    p {
		color: #3b332b;
		font-size: 24px;
		font-weight: 60;
        margin-bottom: 5px;
        margin-top: 2px;
	}
    img {
		width: 120px;
		height: auto;
		border-radius: 10px;
        margin: 5px;
	}
    .promise:hover {
        box-shadow: 0px 0px 10px 0px #000000;
    }
    @media (max-width: 640px) {
        .promise {
            width: 100%;
            height: fit-content;
            border-radius: 10px;
            padding: 10px 10px 20px 10px;
            box-shadow: 0px 0px 5px 0px #000000;
            text-align: center;
        }
        img {
            width: 60%;
        }
    }
</style>