<script>
    import { onMount } from "svelte";

    let filled = 0;

    onMount(() => {
		const inverval = setInterval(() => {
			if (filled <= 100) {
                filled += 1;
            } else {
                filled = 0;
            }
		}, 50);
		return () => clearInterval(inverval);
	});

</script>

<div class="bar">
    <div class="filled" style="width: {filled}%;"></div>
    <div class="empty" style="width: {100-filled}%;"></div>
</div>

<style>
    .bar {
        display: flex;
        width: 90%;
        height: 30px;
        border: 1px solid black;
        border-radius: 30px;
        background-color: antiquewhite;
    }
    .filled {
        background-color: burlywood;
        border-radius: 30px;
    }
    .empty {
        background-color: antiquewhite;
        border-radius: 30px;
    }
</style>