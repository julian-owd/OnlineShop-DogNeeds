<script>
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";

    export let categoryName;

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let newCategoryName = categoryName;
    let inputElement;
    let isEdit = false;

    /**
     * enables / disables the input field
     * changes the name of a category when disabling edit again
     */
    async function toggleEdit() {
        if (isEdit) {
            isEdit = false;
            inputElement.removeAttribute("enabled");
            inputElement.setAttribute("disabled", true);

            // the new name can't be empty and can't be the same as the old name
            if (newCategoryName && newCategoryName !== categoryName) {
                await fetch("http://" + serverAddress + "/category/edit", {
                    method: "POST",
                    body: JSON.stringify({
                        session,
                        categoryName,
                        newCategoryName
                    })
                });
                location.reload();
            }
        } else {
            isEdit = true;
            inputElement.removeAttribute("disabled");
            inputElement.setAttribute("enabled", true);
        }
    }

    /**
     * deletes a category
     */
    async function deleteCategory() {
        const confirmation = window.confirm("Achtung das Löschen der Kategorie kann nicht rückgängig gemacht werden! Produkte dieser Kategorie müssen neu zugewiesen werden!");
        if (confirmation) {
            await fetch("http://" + serverAddress + "/category/remove", {
                method: "POST",
                body: JSON.stringify({
                    session,
                    categoryName
                })
            });
            location.reload();
        }
    }
</script>

<div class="category">
    <input type="text" bind:value={newCategoryName} placeholder={categoryName} disabled bind:this={inputElement}>
    <button on:click={toggleEdit}>
        <img src="/images/admin/edit.png" alt="Edit category"> <!--https://img.icons8.com/ios-filled/512/pencil.png-->
    </button>
    <button on:click={deleteCategory}>
        <img src="/images/admin/delete.png" alt="Delete category"> <!--https://img.icons8.com/ios/512/delete-sign.png-->
    </button>
</div>

<style>
    .category {
        display: flex;
        height: 40px;
        margin: 10px;
    }
    button {
        border: none;
        background-color: white;
    }
    button:hover {
        cursor: pointer;
    }
    input {
        font-size: 24px;
        width: 80%;
        margin-right: 5px;
    }
    input:enabled {
        border: none;
        padding: 5px;
        border-bottom: 2px solid #66bb6a;
    }
    input:disabled {
        cursor: not-allowed;
        padding: 5px;
        border: none;
        border-bottom: 1px solid lightgray;
        background-color: white;
    }
    img {
        width: 40px;
        height: auto;
    }
</style>